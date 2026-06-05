from __future__ import annotations

from typing import Any

import httpx
from tenacity import retry, stop_after_attempt, wait_fixed

from emiyaoj_agent.core.models import AgentResult


class JudgeClientError(RuntimeError):
    pass


class JudgeClient:
    def __init__(self, base_url: str, internal_token: str, timeout_seconds: float = 10.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._headers = {"X-Judge-Internal-Token": internal_token}
        self._timeout_seconds = timeout_seconds

    @retry(stop=stop_after_attempt(3), wait=wait_fixed(1), reraise=True)
    def get_feedback_context(self, submission_id: int) -> dict[str, Any]:
        url = f"{self._base_url}/judge/internal/feedback/context/{submission_id}"
        with httpx.Client(timeout=self._timeout_seconds) as client:
            response = client.get(url, headers=self._headers)
        return self._parse_response(response, "load feedback context")

    @retry(stop=stop_after_attempt(3), wait=wait_fixed(1), reraise=True)
    def submit_feedback(self, result: AgentResult) -> dict[str, Any]:
        url = f"{self._base_url}/judge/internal/feedback"
        with httpx.Client(timeout=self._timeout_seconds) as client:
            response = client.post(
                url,
                headers=self._headers,
                json=result.to_callback_payload(),
            )
        return self._parse_response(response, "submit feedback")

    @staticmethod
    def _parse_response(response: httpx.Response, action: str) -> dict[str, Any]:
        try:
            response.raise_for_status()
            payload = response.json()
        except Exception as exc:
            raise JudgeClientError(f"Judge {action} failed: {exc}") from exc

        if payload.get("code") != 200:
            raise JudgeClientError(f"Judge {action} failed: {payload.get('message')}")
        data = payload.get("data")
        return data if isinstance(data, dict) else {}
