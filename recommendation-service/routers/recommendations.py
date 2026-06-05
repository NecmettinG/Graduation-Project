"""
Recommendation API endpoints.

These endpoints are called by smarty-commerce (proxied through RecommendationController)
and authenticated via JWT using the shared HMAC secret.
"""

import logging
from datetime import datetime, timezone

import jwt  # type: ignore[import-untyped]
from fastapi import APIRouter, HTTPException, Header, Query

from config import TOKEN_SECRET, DEFAULT_TOP_N
from models import RecommendationResponse

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/recommendations", tags=["Recommendations"])

# The engine instance is injected from main.py via the app state
_engine = None


def set_engine(engine):
    """Called by main.py to inject the engine instance."""
    global _engine
    _engine = engine


def _verify_jwt(authorization: str) -> dict:
    """
    Verify the JWT token from the Authorization header.
    The token must be signed with the same HMAC secret used by smarty-commerce.
    """
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing or invalid Authorization header")

    token = authorization.replace("Bearer ", "")
    try:
        payload = jwt.decode(token, TOKEN_SECRET, algorithms=["HS256", "HS384", "HS512"])
        return payload
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token has expired")
    except jwt.InvalidTokenError as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")


@router.get("/product/{product_id}", response_model=RecommendationResponse)
async def get_product_recommendations(
    product_id: str,
    top_n: int = Query(default=DEFAULT_TOP_N, ge=1, le=50),
    authorization: str = Header(None),
):
    """
    Get item-to-item recommendations for a specific product.

    Returns the top-N most similar products based on co-purchase patterns
    computed via cosine similarity on the weighted interaction matrix.
    """
    # Product recommendations are public, no JWT verification needed.
    # _verify_jwt(authorization)

    if _engine is None or not _engine.is_ready:
        raise HTTPException(status_code=503, detail="Recommendation engine is not ready yet")

    recommendations = _engine.get_recommendations(product_id, top_n)

    return RecommendationResponse(
        sourceProductId=product_id,
        recommendations=recommendations,
        generatedAt=datetime.now(timezone.utc),
    )


@router.get("/user/{user_id}", response_model=RecommendationResponse)
async def get_user_recommendations(
    user_id: str,
    top_n: int = Query(default=DEFAULT_TOP_N, ge=1, le=50),
    authorization: str = Header(None),
):
    """
    Get personalized recommendations for a user.

    Aggregates similarity scores across all products the user has interacted with
    (ordered, wishlisted, or added to cart), then returns the top-N products
    the user hasn't already interacted with.
    """
    _verify_jwt(authorization)

    if _engine is None or not _engine.is_ready:
        raise HTTPException(status_code=503, detail="Recommendation engine is not ready yet")

    recommendations = _engine.get_user_recommendations(user_id, top_n)

    return RecommendationResponse(
        sourceUserId=user_id,
        recommendations=recommendations,
        generatedAt=datetime.now(timezone.utc),
    )
