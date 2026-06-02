"""
Recommendation Service — FastAPI Application Entry Point

A Python microservice that provides Item-to-Item Collaborative Filtering
recommendations for the Smarty Commerce e-commerce platform.
"""

import logging
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config import PORT
from engine import RecommendationEngine
from data_fetcher import fetch_interaction_data
from scheduler import start_scheduler, stop_scheduler, rebuild_job
from routers import recommendations as rec_router
from routers import admin as admin_router

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)

# Global engine instance
engine = RecommendationEngine()


async def _rebuild_callback():
    """Callback for the admin manual rebuild endpoint."""
    interaction_data = await fetch_interaction_data()
    engine.build_matrix(interaction_data)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifespan handler.
    - On startup: perform initial data fetch + matrix build, then start scheduler.
    - On shutdown: stop the scheduler gracefully.
    """
    logger.info("=== Recommendation Service Starting ===")

    # Inject the engine into routers
    rec_router.set_engine(engine)
    admin_router.set_engine(engine)
    admin_router.set_rebuild_callback(_rebuild_callback)

    # Initial build — fetch data and build the similarity matrix
    try:
        logger.info("Performing initial data fetch and matrix build...")
        interaction_data = await fetch_interaction_data()
        engine.build_matrix(interaction_data)
        logger.info("Initial matrix build complete.")
    except Exception as e:
        logger.warning(
            "Initial data fetch failed (smarty-commerce may not be running yet): %s. "
            "The engine will be populated on the next scheduled rebuild.",
            str(e),
        )

    # Start the periodic rebuild scheduler
    start_scheduler(engine)

    logger.info("=== Recommendation Service Ready ===")

    yield  # Application is running

    # Shutdown
    logger.info("=== Recommendation Service Shutting Down ===")
    stop_scheduler()


# Create the FastAPI app
app = FastAPI(
    title="Smarty Commerce Recommendation Service",
    description=(
        "Item-to-Item Collaborative Filtering microservice for Smarty Commerce. "
        "Computes product similarity from purchase, wishlist, and cart interaction data."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — allow smarty-commerce backend to call this service
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(rec_router.router)
app.include_router(admin_router.router)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=PORT, reload=True)
