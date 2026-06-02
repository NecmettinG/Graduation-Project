"""
Item-to-Item Collaborative Filtering Engine

Algorithm Overview:
1. Build a user-item interaction matrix from orders, wishlists, and carts.
   - Each cell = sum of weighted signals (order=3, wishlist=2, cart=1).
2. Compute item-item cosine similarity using sklearn.
   - similarity(item_i, item_j) = dot(col_i, col_j) / (||col_i|| * ||col_j||)
3. For a given product, return the top-N most similar products by score.
4. For a given user, aggregate similarities across their interacted items.
"""

import logging
from datetime import datetime, timezone

import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

from config import WEIGHT_ORDER, WEIGHT_WISHLIST, WEIGHT_CART, DEFAULT_TOP_N
from models import InteractionData, RecommendationItem

logger = logging.getLogger(__name__)


class RecommendationEngine:
    """
    Core engine that maintains the item-item similarity matrix in memory
    and serves recommendation queries.
    """

    def __init__(self):
        # Index mappings: product_id <-> matrix column index
        self._product_to_idx: dict[str, int] = {}
        self._idx_to_product: dict[int, str] = {}

        # Index mappings: user_id <-> matrix row index
        self._user_to_idx: dict[str, int] = {}

        # The user-item interaction matrix (n_users x n_products)
        self._interaction_matrix: np.ndarray | None = None

        # The item-item similarity matrix (n_products x n_products)
        self._similarity_matrix: np.ndarray | None = None

        # Tracks which products each user interacted with (for user-based recs)
        self._user_products: dict[str, set[str]] = {}

        # Metadata
        self._last_rebuilt_at: datetime | None = None
        self._is_ready: bool = False

    @property
    def is_ready(self) -> bool:
        return self._is_ready

    @property
    def last_rebuilt_at(self) -> datetime | None:
        return self._last_rebuilt_at

    @property
    def product_count(self) -> int:
        return len(self._product_to_idx)

    @property
    def user_count(self) -> int:
        return len(self._user_to_idx)

    @property
    def matrix_density(self) -> float:
        """Fraction of non-zero entries in the interaction matrix."""
        if self._interaction_matrix is None:
            return 0.0
        total = self._interaction_matrix.size
        if total == 0:
            return 0.0
        non_zero = np.count_nonzero(self._interaction_matrix)
        return round(non_zero / total, 4)

    def build_matrix(self, interaction_data: InteractionData) -> None:
        """
        Build the user-item interaction matrix and compute the item-item
        cosine similarity matrix from the provided interaction data.
        """
        start_time = datetime.now(timezone.utc)
        logger.info("Starting similarity matrix build...")

        # Step 1: Collect all unique product IDs and user IDs
        all_products: set[str] = set()
        all_users: set[str] = set()
        self._user_products = {}

        for order in interaction_data.orders:
            all_users.add(order.userId)
            all_products.update(order.productIds)
            self._user_products.setdefault(order.userId, set()).update(order.productIds)

        for wishlist in interaction_data.wishlists:
            all_users.add(wishlist.userId)
            all_products.update(wishlist.productIds)
            self._user_products.setdefault(wishlist.userId, set()).update(wishlist.productIds)

        for cart in interaction_data.carts:
            all_users.add(cart.userId)
            all_products.update(cart.productIds)
            self._user_products.setdefault(cart.userId, set()).update(cart.productIds)

        if not all_products or not all_users:
            logger.warning("No interaction data available. Matrix not built.")
            self._is_ready = False
            return

        # Step 2: Build index mappings
        sorted_products = sorted(all_products)
        sorted_users = sorted(all_users)

        self._product_to_idx = {pid: idx for idx, pid in enumerate(sorted_products)}
        self._idx_to_product = {idx: pid for pid, idx in self._product_to_idx.items()}
        self._user_to_idx = {uid: idx for idx, uid in enumerate(sorted_users)}

        n_users = len(sorted_users)
        n_products = len(sorted_products)

        # Step 3: Build the interaction matrix with weighted signals
        matrix = np.zeros((n_users, n_products), dtype=np.float32)

        for order in interaction_data.orders:
            user_idx = self._user_to_idx[order.userId]
            for pid in order.productIds:
                product_idx = self._product_to_idx[pid]
                matrix[user_idx][product_idx] += WEIGHT_ORDER

        for wishlist in interaction_data.wishlists:
            user_idx = self._user_to_idx[wishlist.userId]
            for pid in wishlist.productIds:
                product_idx = self._product_to_idx[pid]
                matrix[user_idx][product_idx] += WEIGHT_WISHLIST

        for cart in interaction_data.carts:
            user_idx = self._user_to_idx[cart.userId]
            for pid in cart.productIds:
                product_idx = self._product_to_idx[pid]
                matrix[user_idx][product_idx] += WEIGHT_CART

        self._interaction_matrix = matrix

        # Step 4: Compute item-item cosine similarity
        # Transpose so each column becomes a product's interaction vector,
        # then compute pairwise cosine similarity between all product pairs.
        self._similarity_matrix = cosine_similarity(matrix.T)

        # Zero out self-similarity on the diagonal
        np.fill_diagonal(self._similarity_matrix, 0.0)

        self._is_ready = True
        self._last_rebuilt_at = datetime.now(timezone.utc)

        elapsed = (self._last_rebuilt_at - start_time).total_seconds()
        logger.info(
            "Matrix build complete in %.2fs — %d users, %d products, density=%.4f",
            elapsed, n_users, n_products, self.matrix_density,
        )

    def get_recommendations(self, product_id: str, top_n: int = DEFAULT_TOP_N) -> list[RecommendationItem]:
        """
        Get the top-N most similar products to the given product.

        Uses pre-computed cosine similarity scores from the item-item matrix.
        """
        if not self._is_ready or self._similarity_matrix is None:
            logger.warning("Engine not ready. Returning empty recommendations.")
            return []

        if product_id not in self._product_to_idx:
            logger.info("Product '%s' not found in matrix. Returning empty.", product_id)
            return []

        product_idx = self._product_to_idx[product_id]
        similarity_scores = self._similarity_matrix[product_idx]

        # Get indices of top-N highest similarity scores
        # Use argpartition for efficiency, then sort the top-N
        if len(similarity_scores) <= top_n:
            top_indices = np.argsort(similarity_scores)[::-1]
        else:
            # Partial sort: get top_n indices
            partitioned = np.argpartition(similarity_scores, -top_n)[-top_n:]
            # Sort those top_n by actual score (descending)
            top_indices = partitioned[np.argsort(similarity_scores[partitioned])[::-1]]

        recommendations = []
        for idx in top_indices:
            score = float(similarity_scores[idx])
            if score <= 0:
                break  # No more meaningful similarities
            recommendations.append(
                RecommendationItem(
                    productId=self._idx_to_product[idx],
                    score=round(score, 4),
                )
            )

        return recommendations

    def get_user_recommendations(self, user_id: str, top_n: int = DEFAULT_TOP_N) -> list[RecommendationItem]:
        """
        Get personalized recommendations for a user based on their interaction history.

        Aggregates similarity scores across all products the user has interacted with,
        then returns the top-N products the user hasn't already interacted with.
        """
        if not self._is_ready or self._similarity_matrix is None:
            logger.warning("Engine not ready. Returning empty recommendations.")
            return []

        if user_id not in self._user_products:
            logger.info("User '%s' has no interactions. Returning empty.", user_id)
            return []

        interacted_products = self._user_products[user_id]
        n_products = len(self._product_to_idx)

        # Aggregate similarity scores from all products the user interacted with
        aggregated_scores = np.zeros(n_products, dtype=np.float64)
        interacted_indices: set[int] = set()

        for pid in interacted_products:
            if pid in self._product_to_idx:
                idx = self._product_to_idx[pid]
                interacted_indices.add(idx)
                aggregated_scores += self._similarity_matrix[idx]

        # Zero out products the user already interacted with
        for idx in interacted_indices:
            aggregated_scores[idx] = 0.0

        # Get top-N
        if n_products <= top_n:
            top_indices = np.argsort(aggregated_scores)[::-1]
        else:
            partitioned = np.argpartition(aggregated_scores, -top_n)[-top_n:]
            top_indices = partitioned[np.argsort(aggregated_scores[partitioned])[::-1]]

        recommendations = []
        for idx in top_indices:
            score = float(aggregated_scores[idx])
            if score <= 0:
                break
            recommendations.append(
                RecommendationItem(
                    productId=self._idx_to_product[idx],
                    score=round(score, 4),
                )
            )

        return recommendations[:top_n]
