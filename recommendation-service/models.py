from pydantic import BaseModel
from datetime import datetime


# --- Interaction data models (from smarty-commerce data feed) ---

class OrderInteraction(BaseModel):
    userId: str
    productIds: list[str]


class WishlistInteraction(BaseModel):
    userId: str
    productIds: list[str]


class CartInteraction(BaseModel):
    userId: str
    productIds: list[str]


class InteractionData(BaseModel):
    orders: list[OrderInteraction] = []
    wishlists: list[WishlistInteraction] = []
    carts: list[CartInteraction] = []


# --- Recommendation response models ---

class RecommendationItem(BaseModel):
    productId: str
    score: float


class RecommendationResponse(BaseModel):
    sourceProductId: str | None = None
    sourceUserId: str | None = None
    recommendations: list[RecommendationItem]
    generatedAt: datetime
    algorithm: str = "item-item-cf-cosine"


# --- Admin / status models ---

class MatrixStatus(BaseModel):
    productCount: int
    userCount: int
    matrixDensity: float
    lastRebuiltAt: datetime | None
    rebuildIntervalMinutes: int
    isReady: bool


class HealthResponse(BaseModel):
    status: str
    service: str = "recommendation-service"
    engineReady: bool
