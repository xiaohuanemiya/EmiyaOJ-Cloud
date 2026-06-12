from __future__ import annotations

import logging

from emiyaoj_agent.app import build_registry
from emiyaoj_agent.core.settings import load_settings
from emiyaoj_agent.workers.rabbit_worker import RabbitAgentWorker


def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
    )
    settings = load_settings()
    logger = logging.getLogger(__name__)
    if settings.llm_api_key:
        logger.info(
            "Judge feedback LLM is enabled, model=%s, readTimeoutSeconds=%s",
            settings.judge_feedback_model,
            settings.judge_feedback_timeout_seconds,
        )
    else:
        logger.warning(
            "Judge feedback DeepSeek LLM is disabled because no API key is configured; "
            "set DEEPSEEK_API_KEY, JUDGE_FEEDBACK_API_KEY, or CHAT_API_KEY"
        )
    registry = build_registry(settings)
    RabbitAgentWorker(settings, registry).run()


if __name__ == "__main__":
    main()
