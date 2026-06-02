import os
from dotenv import load_dotenv

load_dotenv()

SMARTY_COMMERCE_BASE_URL: str = os.getenv("SMARTY_COMMERCE_BASE_URL", "http://localhost:8080/smarty-commerce")
TOKEN_SECRET: str = os.getenv("TOKEN_SECRET", "")
REBUILD_INTERVAL_MINUTES: int = int(os.getenv("REBUILD_INTERVAL_MINUTES", "30"))
DEFAULT_TOP_N: int = int(os.getenv("DEFAULT_TOP_N", "10"))
PORT: int = int(os.getenv("PORT", "8000"))

# Signal weights for building the user-item interaction matrix
WEIGHT_ORDER: int = 3
WEIGHT_WISHLIST: int = 2
WEIGHT_CART: int = 1
