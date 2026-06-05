from __future__ import annotations

import httpx

from emiyaoj_agent.core.models import LLMOptions
from emiyaoj_agent.llm.base import LLMClientError


class DashScopeClient:
    def __init__(self, api_key: str | None, api_url: str) -> None:
        self._api_key = api_key
        self._api_url = api_url

    def generate(self, messages: list[dict[str, str]], options: LLMOptions) -> str:
        if not self._api_key:
            raise LLMClientError("DashScope API key is not configured")

        payload = {
            "model": options.model,
            "input": {"messages": messages},
            "parameters": {
                "temperature": options.temperature,
                "max_tokens": options.max_tokens,
            },
        }
        headers = {
            "Authorization": f"Bearer {self._api_key}",
            "Content-Type": "application/json",
            "X-DashScope-SSE": "disable",
        }
        try:
            with httpx.Client(timeout=options.timeout_seconds) as client:
                response = client.post(self._api_url, json=payload, headers=headers)
                response.raise_for_status()
                data = response.json()
        except Exception as exc:
            raise LLMClientError(f"DashScope request failed: {exc}") from exc

        content = self._extract_content(data)
        if not content:
            raise LLMClientError("DashScope response does not contain text content")
        return content

    @staticmethod
    def _extract_content(data: dict) -> str | None:
        output = data.get("output") if isinstance(data, dict) else None
        if not isinstance(output, dict):
            return None

        text = output.get("text")
        if isinstance(text, str) and text.strip():
            return text

        choices = output.get("choices")
        if isinstance(choices, list) and choices:
            message = choices[0].get("message") if isinstance(choices[0], dict) else None
            content = message.get("content") if isinstance(message, dict) else None
            if isinstance(content, str) and content.strip():
                return content
        return None
