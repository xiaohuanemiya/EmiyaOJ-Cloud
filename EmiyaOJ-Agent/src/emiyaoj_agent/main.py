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
    registry = build_registry(settings)
    RabbitAgentWorker(settings, registry).run()


if __name__ == "__main__":
    main()
