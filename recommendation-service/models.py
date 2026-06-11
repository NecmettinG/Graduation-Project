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


# --- Product catalog models (for Content-Based Filtering) ---

class ProductCatalogItem(BaseModel):
    """
    Lightweight product metadata used by the Content-Based Filtering component.
    Only contains the features needed for CBF similarity computation.
    """
    productId: str
    categoryName: str = ""
    mainCategoryName: str = ""
    brand: str = ""
    price: float = 0.0


# --- Recommendation response models ---

class RecommendationItem(BaseModel):
    productId: str
    score: float


class RecommendationResponse(BaseModel):
    sourceProductId: str | None = None
    sourceUserId: str | None = None
    recommendations: list[RecommendationItem]
    generatedAt: datetime
    algorithm: str = "hybrid-cf-cbf-cosine"


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
