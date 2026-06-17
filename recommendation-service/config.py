import os
from dotenv import load_dotenv

basedir = os.path.abspath(os.path.dirname(__file__))
load_dotenv(os.path.join(basedir, '.env'))

SMARTY_COMMERCE_BASE_URL: str = os.getenv("SMARTY_COMMERCE_BASE_URL", "http://localhost:8080/smarty-commerce")
TOKEN_SECRET: str = os.getenv("TOKEN_SECRET", "")
REBUILD_INTERVAL_MINUTES: int = int(os.getenv("REBUILD_INTERVAL_MINUTES", "30"))
DEFAULT_TOP_N: int = int(os.getenv("DEFAULT_TOP_N", "10"))
PORT: int = int(os.getenv("PORT", "8000"))

# Signal weights for building the user-item interaction matrix (Collaborative Filtering)
WEIGHT_ORDER: int = 3
WEIGHT_WISHLIST: int = 2
WEIGHT_CART: int = 1

# Hybrid blending: α controls the mix between CF and CBF.
# final_score = ALPHA_CF * cf_score + (1 - ALPHA_CF) * cbf_score
# Set to 0.7 = 70% CF, 30% CBF for warm products.
# Cold-start products (no CF signal) use 100% CBF automatically.
ALPHA_CF: float = float(os.getenv("ALPHA_CF", "0.7"))
