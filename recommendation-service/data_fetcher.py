import logging

import httpx
import jwt  # type: ignore[import-untyped]

from config import SMARTY_COMMERCE_BASE_URL, TOKEN_SECRET
from models import InteractionData

logger = logging.getLogger(__name__)


def _generate_service_jwt() -> str:
    """
    Generate a JWT token signed with the same HMAC secret used by smarty-commerce.
    The subject is set to 'recommendation-service' so the Java side can identify
    the caller as an internal service rather than a regular user.
    """
    import time
    payload = {
        "sub": "recommendation-service",
        "iat": int(time.time()),
        "exp": int(time.time()) + 3600,  # 1 hour expiry
        "service": True,
    }
    return jwt.encode(payload, TOKEN_SECRET, algorithm="HS384")


async def fetch_interaction_data() -> InteractionData:
    """
    Fetch aggregated interaction data (orders, wishlists, carts) from
    smarty-commerce's internal data-feed endpoint.

    Returns an InteractionData object ready for the recommendation engine.
    """
    url = f"{SMARTY_COMMERCE_BASE_URL}/internal/data-feed/interactions"
    token = _generate_service_jwt()
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/json",
    }

    logger.info("Fetching interaction data from %s", url)

    async with httpx.AsyncClient(timeout=60.0) as client:
        try:
            response = await client.get(url, headers=headers)
            response.raise_for_status()
            data = response.json()
            interaction_data = InteractionData(**data)
            logger.info(
                "Fetched interaction data: %d orders, %d wishlists, %d carts",
                len(interaction_data.orders),
                len(interaction_data.wishlists),
                len(interaction_data.carts),
            )
            return interaction_data
        except httpx.HTTPStatusError as e:
            logger.error("HTTP error fetching data feed: %s — %s", e.response.status_code, e.response.text)
            raise
        except httpx.RequestError as e:
            logger.error("Connection error fetching data feed: %s", str(e))
            raise
