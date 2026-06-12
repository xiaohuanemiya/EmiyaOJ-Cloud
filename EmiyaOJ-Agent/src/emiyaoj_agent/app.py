from __future__ import annotations

from emiyaoj_agent.agents.judge_feedback.agent import JudgeFeedbackAgent
from emiyaoj_agent.core.registry import AgentRegistry
from emiyaoj_agent.core.settings import Settings
from emiyaoj_agent.llm.deepseek import DeepSeekClient
from emiyaoj_agent.tools.judge_client import JudgeClient


def build_registry(settings: Settings) -> AgentRegistry:
    judge_client = JudgeClient(
        base_url=settings.judge_base_url,
        internal_token=settings.judge_internal_token,
        timeout_seconds=settings.judge_http_timeout_seconds,
    )
    llm_client = DeepSeekClient(
        api_key=settings.llm_api_key,
        api_url=settings.judge_feedback_api_url,
    )
    registry = AgentRegistry()
    registry.register(
        JudgeFeedbackAgent(
            judge_client=judge_client,
            llm_client=llm_client,
            model=settings.judge_feedback_model,
            timeout_seconds=settings.judge_feedback_timeout_seconds,
            max_tokens=settings.judge_feedback_max_tokens,
        )
    )
    return registry
