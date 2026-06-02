"""
Unit tests for the RecommendationEngine.

Tests the core algorithm: matrix construction, cosine similarity computation,
product-based recommendations, and user-based recommendations.
"""

import pytest

from engine import RecommendationEngine
from models import InteractionData, OrderInteraction, WishlistInteraction, CartInteraction


def _build_sample_data() -> InteractionData:
    """
    Create sample interaction data that models a clear co-purchase pattern:

    - User A ordered products P1, P2, P3
    - User B ordered products P1, P2
    - User C ordered products P3, P4
    - User D wishlisted P1, P2
    - User A has P4 in cart

    Expected: P1 and P2 should be highly similar (co-purchased by A and B).
              P3 and P4 should have some similarity (co-purchased by C, cart by A).
              P1/P2 and P4 should have weak similarity.
    """
    return InteractionData(
        orders=[
            OrderInteraction(userId="userA", productIds=["P1", "P2", "P3"]),
            OrderInteraction(userId="userB", productIds=["P1", "P2"]),
            OrderInteraction(userId="userC", productIds=["P3", "P4"]),
        ],
        wishlists=[
            WishlistInteraction(userId="userD", productIds=["P1", "P2"]),
        ],
        carts=[
            CartInteraction(userId="userA", productIds=["P4"]),
        ],
    )


class TestRecommendationEngine:

    def test_engine_starts_not_ready(self):
        engine = RecommendationEngine()
        assert engine.is_ready is False
        assert engine.product_count == 0
        assert engine.user_count == 0

    def test_build_matrix_with_valid_data(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        assert engine.is_ready is True
        assert engine.product_count == 4  # P1, P2, P3, P4
        assert engine.user_count == 4     # userA, userB, userC, userD
        assert engine.last_rebuilt_at is not None

    def test_build_matrix_with_empty_data(self):
        engine = RecommendationEngine()
        data = InteractionData(orders=[], wishlists=[], carts=[])
        engine.build_matrix(data)

        assert engine.is_ready is False

    def test_matrix_density_is_valid(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        density = engine.matrix_density
        assert 0.0 < density <= 1.0

    def test_product_recommendations_returns_results(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_recommendations("P1", top_n=3)
        assert len(recs) > 0
        # All scores should be between 0 and 1
        for rec in recs:
            assert 0.0 < rec.score <= 1.0

    def test_p1_and_p2_are_highly_similar(self):
        """P1 and P2 are co-purchased by userA and userB, and co-wishlisted by userD."""
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs_for_p1 = engine.get_recommendations("P1", top_n=3)
        product_ids = [r.productId for r in recs_for_p1]
        # P2 should be the most similar to P1
        assert product_ids[0] == "P2"

    def test_unknown_product_returns_empty(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_recommendations("NONEXISTENT", top_n=5)
        assert recs == []

    def test_user_recommendations_returns_results(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_user_recommendations("userC", top_n=3)
        assert len(recs) > 0
        # UserC ordered P3 and P4, so recommendations should NOT include P3 or P4
        rec_ids = {r.productId for r in recs}
        assert "P3" not in rec_ids
        assert "P4" not in rec_ids

    def test_user_recommendations_excludes_interacted_items(self):
        """UserA ordered P1, P2, P3 and has P4 in cart — no recs should overlap."""
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_user_recommendations("userA", top_n=5)
        rec_ids = {r.productId for r in recs}
        # userA interacted with all 4 products, so no recs
        assert "P1" not in rec_ids
        assert "P2" not in rec_ids
        assert "P3" not in rec_ids
        assert "P4" not in rec_ids

    def test_unknown_user_returns_empty(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_user_recommendations("NONEXISTENT", top_n=5)
        assert recs == []

    def test_engine_not_ready_returns_empty(self):
        engine = RecommendationEngine()
        recs = engine.get_recommendations("P1", top_n=5)
        assert recs == []

        recs = engine.get_user_recommendations("userA", top_n=5)
        assert recs == []

    def test_scores_are_sorted_descending(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_recommendations("P1", top_n=10)
        scores = [r.score for r in recs]
        assert scores == sorted(scores, reverse=True)

    def test_top_n_limits_results(self):
        engine = RecommendationEngine()
        data = _build_sample_data()
        engine.build_matrix(data)

        recs = engine.get_recommendations("P1", top_n=1)
        assert len(recs) == 1

    def test_weighted_signals_affect_similarity(self):
        """
        Orders (weight=3) should produce stronger similarity than carts (weight=1).
        Test: Create two pairs — one connected by orders, one by carts only.
        """
        data = InteractionData(
            orders=[
                OrderInteraction(userId="u1", productIds=["A", "B"]),
                OrderInteraction(userId="u2", productIds=["A", "B"]),
            ],
            wishlists=[],
            carts=[
                CartInteraction(userId="u3", productIds=["A", "C"]),
                CartInteraction(userId="u4", productIds=["A", "C"]),
            ],
        )
        engine = RecommendationEngine()
        engine.build_matrix(data)

        recs = engine.get_recommendations("A", top_n=2)
        # B should rank higher than C because orders have weight=3 vs cart weight=1
        assert recs[0].productId == "B"
        assert recs[1].productId == "C"
        assert recs[0].score > recs[1].score
