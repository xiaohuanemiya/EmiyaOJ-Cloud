from __future__ import annotations

from typing import Protocol

from emiyaoj_agent.core.models import LLMOptions


class LLMClient(Protocol):
    def generate(self, messages: list[dict[str, str]], options: LLMOptions) -> str:
        ...


class LLMClientError(RuntimeError):
    pass
