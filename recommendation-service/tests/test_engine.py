"""
Unit tests for the Hybrid RecommendationEngine.

Tests the core algorithms:
- Collaborative Filtering: matrix construction, cosine similarity
- Content-Based Filtering: feature extraction, category/brand similarity
- Hybrid blending: α-weighted combination, cold-start fallback
- Product-based and user-based recommendation queries
"""

import pytest

from engine import RecommendationEngine
from models import InteractionData, OrderInteraction, WishlistInteraction, CartInteraction, ProductCatalogItem


# ─── Test Data Builders ──────────────────────────────────────────


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


def _build_sample_catalog() -> list[ProductCatalogItem]:
    """
    Create a sample product catalog for CBF testing.
    P1, P2 = Electronics / Phones (same category + same brand = highest similarity)
    P3 = Electronics / Computers (same mainCategory, different sub-category)
    P4 = Clothing / T-Shirt (completely different category)
    """
    return [
        ProductCatalogItem(productId="P1", mainCategoryName="Electronics", categoryName="Phones", brand="Apple", price=50000),
        ProductCatalogItem(productId="P2", mainCategoryName="Electronics", categoryName="Phones", brand="Apple", price=45000),
        ProductCatalogItem(productId="P3", mainCategoryName="Electronics", categoryName="Computers", brand="Apple", price=90000),
        ProductCatalogItem(productId="P4", mainCategoryName="Clothing", categoryName="T-Shirt", brand="Nike", price=1500),
    ]


def _build_cold_start_catalog() -> list[ProductCatalogItem]:
    """
    Catalog including a cold-start product (P5) that has NO interactions.
    P5 is in the same category as P1/P2 — should be recommended via CBF.
    """
    return _build_sample_catalog() + [
        ProductCatalogItem(productId="P5", mainCategoryName="Electronics", categoryName="Phones", brand="Samsung", price=55000),
    ]


# ─── CF Tests (unchanged from v1) ───────────────────────────────


class TestCollaborativeFiltering:
    """Tests for the CF component of the hybrid engine."""

    def test_build_matrix_sets_ready(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        assert engine.is_ready is True

    def test_build_matrix_counts(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        assert engine.product_count == 4  # P1, P2, P3, P4
        assert engine.user_count == 4     # userA, userB, userC, userD

    def test_build_matrix_density(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        assert engine.matrix_density > 0

    def test_similar_products_ranked(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_recommendations("P1", top_n=3)
        assert len(recs) > 0
        product_ids = [r.productId for r in recs]
        assert "P2" in product_ids, "P2 should be similar to P1 (co-purchased by A and B)"

    def test_copurchased_products_most_similar(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_recommendations("P1", top_n=3)
        assert recs[0].productId == "P2", "P2 should be the most similar to P1"

    def test_unknown_product_returns_empty(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_recommendations("UNKNOWN", top_n=5)
        assert recs == []

    def test_engine_not_ready_returns_empty(self):
        engine = RecommendationEngine()
        recs = engine.get_recommendations("P1", top_n=5)
        assert recs == []

    def test_scores_are_descending(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_recommendations("P1", top_n=3)
        scores = [r.score for r in recs]
        assert scores == sorted(scores, reverse=True)


# ─── User Recommendation Tests ──────────────────────────────────


class TestUserRecommendations:
    """Tests for user-based recommendation queries."""

    def test_user_gets_recommendations(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("userB", top_n=5)
        assert len(recs) > 0, "userB should get recommendations based on P1, P2"

    def test_user_ordered_products_excluded(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("userB", top_n=5)
        rec_ids = [r.productId for r in recs]
        assert "P1" not in rec_ids, "P1 was ordered by userB — should be excluded"
        assert "P2" not in rec_ids, "P2 was ordered by userB — should be excluded"

    def test_wishlisted_items_not_excluded(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("userD", top_n=5)
        rec_ids = [r.productId for r in recs]
        assert "P1" in rec_ids or "P2" in rec_ids, (
            "userD wishlisted P1 and P2 — they should appear in recommendations"
        )

    def test_carted_items_not_excluded(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("userA", top_n=5)
        rec_ids = [r.productId for r in recs]
        assert "P4" in rec_ids, (
            "P4 is in userA's cart (not ordered) — should appear in recommendations"
        )

    def test_unknown_user_gets_popularity_fallback(self):
        """New users with no interactions should get popularity-based recommendations."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("UNKNOWN_USER", top_n=5)
        assert len(recs) > 0, "Unknown user should get popularity-based recommendations"

    def test_popularity_fallback_returns_most_popular(self):
        """Popularity fallback should return products with the most interactions."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("BRAND_NEW_USER", top_n=4)
        rec_ids = [r.productId for r in recs]
        # P1 is the most interacted: ordered by A+B, wishlisted by D = weight 3+3+2 = 8
        # P2 is similar: ordered by A+B, wishlisted by D = weight 3+3+2 = 8
        assert "P1" in rec_ids, "P1 (most interactions) should appear in popularity fallback"
        assert "P2" in rec_ids, "P2 (most interactions) should appear in popularity fallback"

    def test_user_scores_descending(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())
        recs = engine.get_user_recommendations("userB", top_n=5)
        scores = [r.score for r in recs]
        assert scores == sorted(scores, reverse=True)


# ─── Content-Based Filtering Tests ──────────────────────────────


class TestContentBasedFiltering:
    """Tests for the CBF component of the hybrid engine."""

    def test_cbf_same_category_high_similarity(self):
        """Products in the same category should have high CBF similarity."""
        engine = RecommendationEngine()
        catalog = _build_sample_catalog()
        # Build with empty interactions to get pure CBF
        engine.build_matrix(InteractionData(), catalog)

        recs = engine.get_recommendations("P1", top_n=3)
        assert len(recs) > 0
        # P2 is same mainCategory + category + brand as P1 → highest CBF similarity
        assert recs[0].productId == "P2", "P2 (same category + brand) should be most similar to P1"

    def test_cbf_same_main_category_moderate_similarity(self):
        """Products in the same mainCategory but different sub-category should still be related."""
        engine = RecommendationEngine()
        catalog = _build_sample_catalog()
        engine.build_matrix(InteractionData(), catalog)

        recs = engine.get_recommendations("P1", top_n=3)
        rec_ids = [r.productId for r in recs]
        assert "P3" in rec_ids, "P3 (Electronics/Computers) should be related to P1 (Electronics/Phones)"

    def test_cbf_different_category_low_similarity(self):
        """Products in completely different categories should have low or no similarity."""
        engine = RecommendationEngine()
        catalog = _build_sample_catalog()
        engine.build_matrix(InteractionData(), catalog)

        recs = engine.get_recommendations("P1", top_n=3)
        rec_ids = [r.productId for r in recs]
        rec_scores = {r.productId: r.score for r in recs}

        # P4 (Clothing/T-Shirt) should have much lower score than P2 (Electronics/Phones)
        if "P4" in rec_ids:
            assert rec_scores["P4"] < rec_scores.get("P2", 1.0), (
                "P4 (Clothing) should have lower score than P2 (same category)"
            )

    def test_cbf_only_mode_works(self):
        """Engine should work with only catalog data and no interactions."""
        engine = RecommendationEngine()
        catalog = _build_sample_catalog()
        engine.build_matrix(InteractionData(), catalog)

        assert engine.is_ready is True
        assert engine.product_count == 4
        # User count is 0 since no interactions
        assert engine.user_count == 0

    def test_cbf_catalog_products_indexed(self):
        """All products from the catalog should be in the index, even without interactions."""
        engine = RecommendationEngine()
        catalog = _build_sample_catalog()
        engine.build_matrix(InteractionData(), catalog)

        for item in catalog:
            assert item.productId in engine._product_to_idx


# ─── Hybrid Blending Tests ──────────────────────────────────────


class TestHybridBlending:
    """Tests for the hybrid CF + CBF blending logic."""

    def test_hybrid_includes_both_signals(self):
        """With both CF and CBF, the hybrid matrix should be non-zero."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_sample_catalog())

        assert engine.is_ready is True
        assert engine._similarity_matrix_cf is not None
        assert engine._similarity_matrix_cbf is not None
        assert engine._similarity_matrix is not None

    def test_hybrid_warm_product_recommendations(self):
        """Warm products (with CF signal) should still produce good recommendations."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_sample_catalog())

        recs = engine.get_recommendations("P1", top_n=3)
        assert len(recs) > 0
        assert recs[0].productId == "P2", "P2 should still be most similar to P1 in hybrid mode"

    def test_cold_start_product_gets_recommendations(self):
        """A cold-start product (no interactions) should get CBF-based recommendations."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_cold_start_catalog())

        # P5 has no interactions but is in Electronics/Phones/Samsung
        recs = engine.get_recommendations("P5", top_n=3)
        assert len(recs) > 0, "Cold-start product P5 should get CBF recommendations"

        rec_ids = [r.productId for r in recs]
        # P5 is Electronics/Phones — should be most similar to P1, P2 (also Electronics/Phones)
        assert "P1" in rec_ids or "P2" in rec_ids, (
            "P5 (Electronics/Phones) should be recommended products from the same category"
        )

    def test_cold_start_product_in_index(self):
        """Cold-start products from catalog should be present in the product index."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_cold_start_catalog())

        assert "P5" in engine._product_to_idx
        assert engine.product_count == 5  # P1-P4 from interactions + P5 from catalog

    def test_cold_start_prefers_same_category(self):
        """Cold-start product should rank same-category products higher."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_cold_start_catalog())

        recs = engine.get_recommendations("P5", top_n=4)
        rec_scores = {r.productId: r.score for r in recs}

        # P1, P2 are same category (Phones) as P5 — should score higher than P4 (Clothing)
        phones_score = max(rec_scores.get("P1", 0), rec_scores.get("P2", 0))
        clothing_score = rec_scores.get("P4", 0)
        assert phones_score > clothing_score, (
            f"Same-category products should score higher: Phones={phones_score} > Clothing={clothing_score}"
        )

    def test_hybrid_no_catalog_falls_back_to_cf(self):
        """Without a catalog, the engine should still work using CF only."""
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data())  # No catalog

        assert engine.is_ready is True
        recs = engine.get_recommendations("P1", top_n=3)
        assert len(recs) > 0
        assert recs[0].productId == "P2"


# ─── Edge Cases ─────────────────────────────────────────────────


class TestEdgeCases:
    """Edge case tests."""

    def test_empty_interaction_data(self):
        engine = RecommendationEngine()
        engine.build_matrix(InteractionData())
        assert engine.is_ready is False

    def test_empty_interaction_with_catalog(self):
        """With only catalog and no interactions, engine should still be ready (CBF-only)."""
        engine = RecommendationEngine()
        engine.build_matrix(InteractionData(), _build_sample_catalog())
        assert engine.is_ready is True

    def test_top_n_larger_than_products(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_sample_catalog())
        recs = engine.get_recommendations("P1", top_n=100)
        assert len(recs) <= engine.product_count

    def test_rebuild_replaces_old_data(self):
        engine = RecommendationEngine()
        engine.build_matrix(_build_sample_data(), _build_sample_catalog())
        first_rebuilt = engine.last_rebuilt_at

        # Rebuild with different data
        engine.build_matrix(
            InteractionData(
                orders=[OrderInteraction(userId="userX", productIds=["P1", "P4"])],
            ),
            _build_sample_catalog(),
        )
        assert engine.last_rebuilt_at != first_rebuilt
