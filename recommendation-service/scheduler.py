"""
Scheduler for periodic similarity matrix rebuilds.

Uses APScheduler's AsyncIOScheduler to run a background job that:
1. Fetches fresh interaction data from smarty-commerce
2. Rebuilds the item-item similarity matrix

Default interval: 30 minutes (configurable via REBUILD_INTERVAL_MINUTES).
"""

import logging

from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger

from config import REBUILD_INTERVAL_MINUTES
from data_fetcher import fetch_interaction_data
from engine import RecommendationEngine

logger = logging.getLogger(__name__)

_scheduler: AsyncIOScheduler | None = None


async def rebuild_job(engine: RecommendationEngine) -> None:
    """Fetch interaction data and rebuild the similarity matrix."""
    try:
        logger.info("Scheduled rebuild started...")
        interaction_data = await fetch_interaction_data()
        engine.build_matrix(interaction_data)
        logger.info("Scheduled rebuild completed successfully.")
    except Exception as e:
        logger.error("Scheduled rebuild failed: %s", str(e), exc_info=True)


def start_scheduler(engine: RecommendationEngine) -> AsyncIOScheduler:
    """Start the APScheduler with a periodic rebuild job."""
    global _scheduler

    _scheduler = AsyncIOScheduler()
    _scheduler.add_job(
        rebuild_job,
        trigger=IntervalTrigger(minutes=REBUILD_INTERVAL_MINUTES),
        args=[engine],
        id="rebuild_similarity_matrix",
        name=f"Rebuild similarity matrix every {REBUILD_INTERVAL_MINUTES} minutes",
        replace_existing=True,
    )
    _scheduler.start()
    logger.info("Scheduler started — rebuild every %d minutes", REBUILD_INTERVAL_MINUTES)
    return _scheduler


def stop_scheduler() -> None:
    """Gracefully shut down the scheduler."""
    global _scheduler
    if _scheduler and _scheduler.running:
        _scheduler.shutdown(wait=False)
        logger.info("Scheduler stopped.")
