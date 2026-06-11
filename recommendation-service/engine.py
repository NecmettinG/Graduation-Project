"""
Hybrid Recommendation Engine — Content-Based + Item-to-Item Collaborative Filtering

Algorithm Overview:
1. **Collaborative Filtering (CF)**:
   - Build a user-item interaction matrix from orders, wishlists, and carts.
   - Each cell = sum of weighted signals (order=3, wishlist=2, cart=1).
   - Compute item-item cosine similarity → CF similarity matrix.

2. **Content-Based Filtering (CBF)**:
   - Build a product feature matrix from catalog metadata.
   - Features: mainCategory (one-hot), categoryName (one-hot), brand (one-hot), price (normalized).
   - Compute item-item cosine similarity → CBF similarity matrix.

3. **Hybrid Blending**:
   - For warm products (with CF signal): final = α × CF + (1-α) × CBF
   - For cold-start products (no CF signal): final = CBF only
   - α defaults to 0.7 (70% CF, 30% CBF).
"""

import logging
from datetime import datetime, timezone
from typing import Optional

import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

from config import WEIGHT_ORDER, WEIGHT_WISHLIST, WEIGHT_CART, DEFAULT_TOP_N, ALPHA_CF
from models import InteractionData, ProductCatalogItem, RecommendationItem

logger = logging.getLogger(__name__)


class RecommendationEngine:
    """
    Hybrid recommendation engine that combines Content-Based Filtering
    with Item-to-Item Collaborative Filtering for robust recommendations
    even for cold-start products.
    """

    def __init__(self):
        # Index mappings: product_id <-> matrix column index
        self._product_to_idx: dict[str, int] = {}
        self._idx_to_product: dict[int, str] = {}

        # Index mappings: user_id <-> matrix row index
        self._user_to_idx: dict[str, int] = {}

        # The user-item interaction matrix (n_users x n_products)
        self._interaction_matrix: np.ndarray | None = None

        # Similarity matrices
        self._similarity_matrix_cf: np.ndarray | None = None     # From interactions
        self._similarity_matrix_cbf: np.ndarray | None = None    # From product features
        self._similarity_matrix: np.ndarray | None = None        # Final hybrid blend

        # Tracks ALL products each user interacted with (for similarity aggregation)
        self._user_products: dict[str, set[str]] = {}

        # Tracks only ORDERED products (for exclusion from user recommendations)
        self._user_ordered_products: dict[str, set[str]] = {}

        # Product catalog for CBF
        self._product_catalog: list[ProductCatalogItem] = []

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

    # ─── Matrix Construction ─────────────────────────────────────

    def build_matrix(
        self,
        interaction_data: InteractionData,
        product_catalog: Optional[list[ProductCatalogItem]] = None,
    ) -> None:
        """
        Build the hybrid similarity matrix from interaction data and product catalog.

        Steps:
        1. Build user-item interaction matrix and compute CF similarity.
        2. If product catalog is available, build CBF similarity from features.
        3. Blend CF and CBF into the final hybrid similarity matrix.
        """
        start_time = datetime.now(timezone.utc)
        logger.info("Starting hybrid similarity matrix build...")

        if product_catalog is not None:
            self._product_catalog = product_catalog

        # Step 1: Collect all unique product IDs and user IDs
        all_products: set[str] = set()
        all_users: set[str] = set()
        self._user_products = {}
        self._user_ordered_products = {}

        for order in interaction_data.orders:
            all_users.add(order.userId)
            all_products.update(order.productIds)
            self._user_products.setdefault(order.userId, set()).update(order.productIds)
            self._user_ordered_products.setdefault(order.userId, set()).update(order.productIds)

        for wishlist in interaction_data.wishlists:
            all_users.add(wishlist.userId)
            all_products.update(wishlist.productIds)
            self._user_products.setdefault(wishlist.userId, set()).update(wishlist.productIds)

        for cart in interaction_data.carts:
            all_users.add(cart.userId)
            all_products.update(cart.productIds)
            self._user_products.setdefault(cart.userId, set()).update(cart.productIds)

        # Include products from catalog that may not have any interactions (cold-start)
        if self._product_catalog:
            for item in self._product_catalog:
                all_products.add(item.productId)

        if not all_products:
            logger.warning("No product data available. Matrix not built.")
            self._is_ready = False
            return

        # Step 2: Build index mappings
        sorted_products = sorted(all_products)
        sorted_users = sorted(all_users) if all_users else []

        self._product_to_idx = {pid: idx for idx, pid in enumerate(sorted_products)}
        self._idx_to_product = {idx: pid for pid, idx in self._product_to_idx.items()}
        self._user_to_idx = {uid: idx for idx, uid in enumerate(sorted_users)}

        n_users = len(sorted_users)
        n_products = len(sorted_products)

        # Step 3: Build CF similarity matrix (from interactions)
        self._similarity_matrix_cf = self._build_cf_matrix(
            interaction_data, n_users, n_products,
        )

        # Step 4: Build CBF similarity matrix (from product features)
        self._similarity_matrix_cbf = self._build_cbf_matrix(n_products)

        # Step 5: Blend CF and CBF into the final hybrid matrix
        self._similarity_matrix = self._blend_matrices(n_products)

        self._is_ready = True
        self._last_rebuilt_at = datetime.now(timezone.utc)

        elapsed = (self._last_rebuilt_at - start_time).total_seconds()
        has_cbf = self._similarity_matrix_cbf is not None
        logger.info(
            "Hybrid matrix build complete in %.2fs — %d users, %d products, "
            "density=%.4f, CBF=%s, alpha=%.2f",
            elapsed, n_users, n_products, self.matrix_density,
            "enabled" if has_cbf else "disabled", ALPHA_CF,
        )

    def _build_cf_matrix(
        self,
        interaction_data: InteractionData,
        n_users: int,
        n_products: int,
    ) -> np.ndarray | None:
        """
        Build the Collaborative Filtering similarity matrix from the
        user-item interaction matrix using cosine similarity.
        """
        if n_users == 0:
            logger.info("No users found. CF matrix will be empty.")
            return None

        # Build the interaction matrix with weighted signals
        matrix = np.zeros((n_users, n_products), dtype=np.float32)

        for order in interaction_data.orders:
            user_idx = self._user_to_idx[order.userId]
            for pid in order.productIds:
                if pid in self._product_to_idx:
                    product_idx = self._product_to_idx[pid]
                    matrix[user_idx][product_idx] += WEIGHT_ORDER

        for wishlist in interaction_data.wishlists:
            user_idx = self._user_to_idx[wishlist.userId]
            for pid in wishlist.productIds:
                if pid in self._product_to_idx:
                    product_idx = self._product_to_idx[pid]
                    matrix[user_idx][product_idx] += WEIGHT_WISHLIST

        for cart in interaction_data.carts:
            user_idx = self._user_to_idx[cart.userId]
            for pid in cart.productIds:
                if pid in self._product_to_idx:
                    product_idx = self._product_to_idx[pid]
                    matrix[user_idx][product_idx] += WEIGHT_CART

        self._interaction_matrix = matrix

        # Compute item-item cosine similarity on the transposed matrix
        cf_sim = cosine_similarity(matrix.T)
        np.fill_diagonal(cf_sim, 0.0)

        return cf_sim

    def _build_cbf_matrix(self, n_products: int) -> np.ndarray | None:
        """
        Build the Content-Based Filtering similarity matrix from product features.

        Features used:
        - mainCategoryName: one-hot encoded (ensures products in the same
          top-level category are considered related)
        - categoryName: one-hot encoded (finer sub-category grouping)
        - brand: one-hot encoded (same-brand products get a similarity boost)
        - price: min-max normalized to [0, 1] (products in similar price ranges
          are considered more related)
        """
        if not self._product_catalog:
            logger.info("No product catalog available. CBF matrix will be empty.")
            return None

        # Build lookup from productId -> catalog item
        catalog_lookup: dict[str, ProductCatalogItem] = {
            item.productId: item for item in self._product_catalog
        }

        # Collect all unique categorical values
        all_main_categories: set[str] = set()
        all_categories: set[str] = set()
        all_brands: set[str] = set()
        all_prices: list[float] = []

        for item in self._product_catalog:
            if item.mainCategoryName:
                all_main_categories.add(item.mainCategoryName)
            if item.categoryName:
                all_categories.add(item.categoryName)
            if item.brand:
                all_brands.add(item.brand)
            all_prices.append(item.price)

        sorted_main_cats = sorted(all_main_categories)
        sorted_cats = sorted(all_categories)
        sorted_brands = sorted(all_brands)

        main_cat_to_idx = {cat: i for i, cat in enumerate(sorted_main_cats)}
        cat_to_idx = {cat: i for i, cat in enumerate(sorted_cats)}
        brand_to_idx = {brand: i for i, brand in enumerate(sorted_brands)}

        # Compute price normalization bounds
        price_min = min(all_prices) if all_prices else 0.0
        price_max = max(all_prices) if all_prices else 1.0
        price_range = price_max - price_min if price_max > price_min else 1.0

        # Feature dimensions
        n_main_cats = len(sorted_main_cats)
        n_cats = len(sorted_cats)
        n_brands = len(sorted_brands)
        n_features = n_main_cats + n_cats + n_brands + 1  # +1 for price

        logger.info(
            "CBF features: %d mainCategories, %d categories, %d brands, 1 price = %d total",
            n_main_cats, n_cats, n_brands, n_features,
        )

        # Build the feature matrix (n_products x n_features)
        feature_matrix = np.zeros((n_products, n_features), dtype=np.float32)

        for pid, idx in self._product_to_idx.items():
            item = catalog_lookup.get(pid)
            if item is None:
                continue  # Product has interactions but no catalog entry

            offset = 0

            # One-hot: mainCategoryName (weight 1.0)
            if item.mainCategoryName and item.mainCategoryName in main_cat_to_idx:
                feature_matrix[idx][offset + main_cat_to_idx[item.mainCategoryName]] = 1.0
            offset += n_main_cats

            # One-hot: categoryName (weight 1.0)
            if item.categoryName and item.categoryName in cat_to_idx:
                feature_matrix[idx][offset + cat_to_idx[item.categoryName]] = 1.0
            offset += n_cats

            # One-hot: brand (weight 0.5 — less important than category)
            if item.brand and item.brand in brand_to_idx:
                feature_matrix[idx][offset + brand_to_idx[item.brand]] = 0.5
            offset += n_brands

            # Normalized price (weight 0.3 — least important)
            normalized_price = (item.price - price_min) / price_range
            feature_matrix[idx][offset] = normalized_price * 0.3

        # Compute cosine similarity on the feature matrix
        cbf_sim = cosine_similarity(feature_matrix)
        np.fill_diagonal(cbf_sim, 0.0)

        return cbf_sim

    def _blend_matrices(self, n_products: int) -> np.ndarray:
        """
        Blend CF and CBF similarity matrices into the final hybrid matrix.

        Strategy:
        - If both CF and CBF are available: final = α × CF + (1-α) × CBF
        - If only CF: use CF as-is
        - If only CBF: use CBF as-is (cold-start fallback)

        For individual product pairs, if a product has NO CF signal at all
        (its entire CF row/column is zero), use 100% CBF for that pair.
        """
        has_cf = self._similarity_matrix_cf is not None
        has_cbf = self._similarity_matrix_cbf is not None

        if has_cf and has_cbf:
            # Identify cold-start products: products with zero CF signal
            # A product is "cold" if its entire row in the CF matrix is zero
            cf_row_sums = np.abs(self._similarity_matrix_cf).sum(axis=1)
            cold_mask = cf_row_sums == 0  # True for cold-start products

            # Start with the standard weighted blend
            hybrid = ALPHA_CF * self._similarity_matrix_cf + (1 - ALPHA_CF) * self._similarity_matrix_cbf

            # For cold-start products, override with 100% CBF
            # If product i is cold-start, its entire row should use CBF
            for i in range(n_products):
                if cold_mask[i]:
                    hybrid[i, :] = self._similarity_matrix_cbf[i, :]
                    hybrid[:, i] = self._similarity_matrix_cbf[:, i]

            np.fill_diagonal(hybrid, 0.0)

            n_cold = int(cold_mask.sum())
            if n_cold > 0:
                logger.info(
                    "Hybrid blending: %d warm products (α=%.2f), %d cold-start products (100%% CBF)",
                    n_products - n_cold, ALPHA_CF, n_cold,
                )

            return hybrid

        elif has_cf:
            logger.info("No CBF available — using CF-only similarity.")
            return self._similarity_matrix_cf

        elif has_cbf:
            logger.info("No CF available — using CBF-only similarity (full cold-start).")
            return self._similarity_matrix_cbf

        else:
            logger.warning("Neither CF nor CBF available. Creating empty similarity matrix.")
            return np.zeros((n_products, n_products), dtype=np.float32)

    # ─── Recommendation Queries ──────────────────────────────────

    def get_recommendations(self, product_id: str, top_n: int = DEFAULT_TOP_N) -> list[RecommendationItem]:
        """
        Get the top-N most similar products to the given product.

        Uses pre-computed hybrid similarity scores (CF + CBF blend).
        Works for both warm products (CF signal) and cold-start products (CBF fallback).
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
        then returns the top-N products the user hasn't already ordered.

        For new users with NO interactions (user cold-start), returns popularity-based
        recommendations — the products with the highest total interaction scores across
        all users.

        Note: Only ORDERED products are excluded. Wishlisted and carted items remain
        eligible since they represent purchase intent — recommending them reinforces
        the user's interest and can drive conversion.
        """
        if not self._is_ready or self._similarity_matrix is None:
            logger.warning("Engine not ready. Returning empty recommendations.")
            return []

        if user_id not in self._user_products:
            logger.info("User '%s' has no interactions. Returning popularity-based recommendations.", user_id)
            return self._get_popular_recommendations(top_n)

        interacted_products = self._user_products[user_id]
        ordered_products = self._user_ordered_products.get(user_id, set())
        n_products = len(self._product_to_idx)

        # Aggregate similarity scores from all products the user interacted with
        aggregated_scores = np.zeros(n_products, dtype=np.float64)

        for pid in interacted_products:
            if pid in self._product_to_idx:
                idx = self._product_to_idx[pid]
                aggregated_scores += self._similarity_matrix[idx]

        # Only zero out products the user has already ORDERED (not wishlisted/carted)
        for pid in ordered_products:
            if pid in self._product_to_idx:
                idx = self._product_to_idx[pid]
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

    def _get_popular_recommendations(self, top_n: int = DEFAULT_TOP_N) -> list[RecommendationItem]:
        """
        Popularity-based fallback for users with no interaction history (user cold-start).

        Computes a popularity score for each product by summing its column in the
        interaction matrix (total weighted interactions across all users).
        Returns the top-N most popular products.
        """
        if self._interaction_matrix is None or self._interaction_matrix.size == 0:
            logger.info("No interaction matrix available for popularity fallback.")
            return []

        # Sum each product's column to get total interaction weight across all users
        popularity_scores = self._interaction_matrix.sum(axis=0)

        n_products = len(self._product_to_idx)

        if n_products <= top_n:
            top_indices = np.argsort(popularity_scores)[::-1]
        else:
            partitioned = np.argpartition(popularity_scores, -top_n)[-top_n:]
            top_indices = partitioned[np.argsort(popularity_scores[partitioned])[::-1]]

        recommendations = []
        for idx in top_indices:
            score = float(popularity_scores[idx])
            if score <= 0:
                break
            recommendations.append(
                RecommendationItem(
                    productId=self._idx_to_product[idx],
                    score=round(score, 4),
                )
            )

        return recommendations[:top_n]

