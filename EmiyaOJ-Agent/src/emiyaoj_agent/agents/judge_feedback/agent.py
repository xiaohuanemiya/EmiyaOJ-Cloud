from __future__ import annotations

import logging

from emiyaoj_agent.agents.judge_feedback.prompts import build_messages, static_fallback
from emiyaoj_agent.core.models import AgentResult, AgentTask, LLMOptions
from emiyaoj_agent.llm.base import LLMClient
from emiyaoj_agent.tools.judge_client import JudgeClient

logger = logging.getLogger(__name__)


class JudgeFeedbackAgent:
    agent_type = "JUDGE_FEEDBACK"

    def __init__(
        self,
        judge_client: JudgeClient,
        llm_client: LLMClient,
        model: str,
        timeout_seconds: float = 15.0,
        max_tokens: int = 1200,
    ) -> None:
        self._judge_client = judge_client
        self._llm_client = llm_client
        self._model = model
        self._timeout_seconds = timeout_seconds
        self._max_tokens = max_tokens

    def handle(self, task: AgentTask) -> AgentResult:
        if task.submission_id is None:
            raise ValueError("submissionId is required for JudgeFeedbackAgent")

        context = self._judge_client.get_feedback_context(task.submission_id)
        try:
            content = self._llm_client.generate(
                build_messages(context),
                LLMOptions(
                    model=self._model,
                    temperature=0.3,
                    max_tokens=self._max_tokens,
                    timeout_seconds=self._timeout_seconds,
                ),
            )
            result = AgentResult(
                submissionId=task.submission_id,
                agentType=self.agent_type,
                status="SUCCESS",
                content=content,
                source="LLM",
                model=self._model,
                traceId=task.trace_id,
            )
        except Exception as exc:
            logger.warning(
                "Judge feedback LLM failed, submissionId=%s, traceId=%s",
                task.submission_id,
                task.trace_id,
                exc_info=True,
            )
            result = AgentResult(
                submissionId=task.submission_id,
                agentType=self.agent_type,
                status="STATIC_FALLBACK",
                content=static_fallback(context),
                source="STATIC_FALLBACK",
                model=self._model,
                traceId=task.trace_id,
                errorMessage=str(exc),
            )

        self._judge_client.submit_feedback(result)
        return result
