from __future__ import annotations

from typing import Protocol

from emiyaoj_agent.core.models import AgentResult, AgentTask


class AgentHandler(Protocol):
    agent_type: str

    def handle(self, task: AgentTask) -> AgentResult:
        ...


class AgentRegistry:
    def __init__(self) -> None:
        self._handlers: dict[str, AgentHandler] = {}

    def register(self, handler: AgentHandler) -> None:
        self._handlers[handler.agent_type] = handler

    def get(self, agent_type: str) -> AgentHandler:
        try:
            return self._handlers[agent_type]
        except KeyError as exc:
            raise KeyError(f"Unsupported agentType: {agent_type}") from exc

    def handle(self, task: AgentTask) -> AgentResult:
        return self.get(task.agent_type).handle(task)

    @property
    def agent_types(self) -> tuple[str, ...]:
        return tuple(sorted(self._handlers))
