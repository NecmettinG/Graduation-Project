"""
Admin and health check endpoints.

The rebuild endpoint allows manual triggering of the similarity matrix rebuild,
which is useful during presentations and testing.
"""

import logging
from datetime import datetime, timezone

import jwt
from fastapi import APIRouter, HTTPException, Header

from config import TOKEN_SECRET, REBUILD_INTERVAL_MINUTES
from models import MatrixStatus, HealthResponse

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Admin"])

# Injected from main.py
_engine = None
_rebuild_callback = None


def set_engine(engine):
    global _engine
    _engine = engine


def set_rebuild_callback(callback):
    """Set the async callback that triggers a full rebuild (fetch + build)."""
    global _rebuild_callback
    _rebuild_callback = callback


def _verify_service_jwt(authorization: str) -> dict:
    """Verify JWT for admin endpoints — must be a service token."""
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


@router.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint — no authentication required."""
    return HealthResponse(
        status="UP",
        engineReady=_engine.is_ready if _engine else False,
    )


@router.get("/admin/status", response_model=MatrixStatus)
async def get_status(authorization: str = Header(None)):
    """Get current status of the recommendation engine and similarity matrix."""
    _verify_service_jwt(authorization)

    if _engine is None:
        raise HTTPException(status_code=503, detail="Engine not initialized")

    return MatrixStatus(
        productCount=_engine.product_count,
        userCount=_engine.user_count,
        matrixDensity=_engine.matrix_density,
        lastRebuiltAt=_engine.last_rebuilt_at,
        rebuildIntervalMinutes=REBUILD_INTERVAL_MINUTES,
        isReady=_engine.is_ready,
    )


@router.post("/admin/rebuild")
async def trigger_rebuild(authorization: str = Header(None)):
    """
    Manually trigger a similarity matrix rebuild.

    Useful during presentations to immediately reflect new orders/interactions.
    """
    _verify_service_jwt(authorization)

    if _rebuild_callback is None:
        raise HTTPException(status_code=503, detail="Rebuild callback not configured")

    try:
        await _rebuild_callback()
        return {
            "status": "success",
            "message": "Similarity matrix rebuilt successfully",
            "rebuiltAt": datetime.now(timezone.utc).isoformat(),
            "productCount": _engine.product_count if _engine else 0,
            "userCount": _engine.user_count if _engine else 0,
        }
    except Exception as e:
        logger.error("Rebuild failed: %s", str(e))
        raise HTTPException(status_code=500, detail=f"Rebuild failed: {str(e)}")
