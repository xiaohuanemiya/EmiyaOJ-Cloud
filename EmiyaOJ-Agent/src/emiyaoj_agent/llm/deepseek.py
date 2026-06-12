from __future__ import annotations

import json
import logging

import httpx

from emiyaoj_agent.core.models import LLMOptions
from emiyaoj_agent.llm.base import LLMClientError

logger = logging.getLogger(__name__)


class DeepSeekClient:
    def __init__(self, api_key: str | None, api_url: str) -> None:
        self._api_key = api_key
        self._api_url = api_url

    def generate(self, messages: list[dict[str, str]], options: LLMOptions) -> str:
        if not self._api_key:
            raise LLMClientError("DeepSeek API key is not configured")

        payload = {
            "model": options.model,
            "messages": messages,
            "temperature": options.temperature,
            "max_tokens": options.max_tokens,
            "stream": False,
        }
        headers = {
            "Authorization": f"Bearer {self._api_key}",
            "Content-Type": "application/json",
        }
        try:
            with httpx.Client(timeout=self._build_timeout(options.timeout_seconds)) as client:
                response = client.post(self._api_url, json=payload, headers=headers)
                response.raise_for_status()
                data = response.json()
        except httpx.TimeoutException as exc:
            raise LLMClientError(
                "DeepSeek request timed out "
                f"({type(exc).__name__}, response read timeout={options.timeout_seconds:g}s)"
            ) from exc
        except httpx.HTTPStatusError as exc:
            error_body = ""
            try:
                error_body = exc.response.text[:1000]
            except Exception:
                pass
            logger.warning(
                "DeepSeek API returned HTTP %d, body=%s",
                exc.response.status_code,
                error_body,
            )
            raise LLMClientError(
                f"DeepSeek API error HTTP {exc.response.status_code}: {error_body}"
            ) from exc
        except Exception as exc:
            raise LLMClientError(f"DeepSeek request failed: {exc}") from exc

        content, error_reason = self._extract_content(data)
        if not content:
            logger.warning(
                "DeepSeek returned empty content, finish_reason=%s, "
                "response_summary=%s",
                error_reason,
                self._summarize_response(data),
            )
            if error_reason == "length":
                raise LLMClientError(
                    "DeepSeek max_tokens (%d) exhausted before any content was "
                    "generated. The prompt may be too long or max_tokens is too "
                    "low for the model's reasoning overhead. "
                    "Consider increasing max_tokens or reducing prompt size."
                    % options.max_tokens
                )
            if error_reason:
                raise LLMClientError(
                    "DeepSeek response has no text content (finish_reason=%s)"
                    % error_reason
                )
            raise LLMClientError("DeepSeek response does not contain text content")
        return content

    @staticmethod
    def _build_timeout(read_timeout_seconds: float) -> httpx.Timeout:
        return httpx.Timeout(
            connect=min(read_timeout_seconds, 10.0),
            read=read_timeout_seconds,
            write=min(read_timeout_seconds, 30.0),
            pool=min(read_timeout_seconds, 10.0),
        )

    @staticmethod
    def _extract_content(data: dict) -> tuple[str | None, str | None]:
        """Extract text content from a DeepSeek chat completion response.

        Returns a tuple of (content, error_reason).
        error_reason is set when no content is found, e.g. 'content_filter', 'length', 'empty_choices'.

        For reasoning models (e.g. deepseek-reasoner, deepseek-v4-pro), the model may
        consume all max_tokens on internal reasoning and leave ``content`` empty.
        In that case we fall back to ``reasoning_content`` so the user at least sees
        the model's thought process.
        """
        if not isinstance(data, dict):
            return None, "response_not_dict"

        choices = data.get("choices")
        if not isinstance(choices, list) or not choices:
            return None, "empty_choices"

        first = choices[0]
        if not isinstance(first, dict):
            return None, "invalid_choice"

        finish_reason = first.get("finish_reason", "")

        message = first.get("message")
        if not isinstance(message, dict):
            # Maybe the response uses delta (streaming) format unexpectedly
            delta = first.get("delta")
            if isinstance(delta, dict) and isinstance(delta.get("content"), str):
                content = delta["content"].strip()
                return (content if content else None, finish_reason or None)
            return None, finish_reason or "no_message"

        content = message.get("content")
        if isinstance(content, str) and content.strip():
            return content.strip(), None

        # content is empty — for reasoning models, fall back to reasoning_content
        reasoning = message.get("reasoning_content")
        if isinstance(reasoning, str) and reasoning.strip():
            logger.info(
                "Using reasoning_content as fallback (finish_reason=%s, "
                "reasoning_len=%d)",
                finish_reason,
                len(reasoning),
            )
            return reasoning.strip(), None

        # No content at all — use finish_reason to explain why
        return None, finish_reason or "empty_content"

    @staticmethod
    def _summarize_response(data: dict, max_chars: int = 500) -> str:
        """Create a short summary of the response for debugging."""
        try:
            summary: dict = {
                "id": data.get("id"),
                "model": data.get("model"),
                "choices": [],
            }
            usage = data.get("usage")
            if isinstance(usage, dict):
                summary["usage"] = {
                    "prompt_tokens": usage.get("prompt_tokens"),
                    "completion_tokens": usage.get("completion_tokens"),
                    "total_tokens": usage.get("total_tokens"),
                }
            for choice in (data.get("choices") or [])[:1]:
                c: dict = {}
                if isinstance(choice, dict):
                    c["finish_reason"] = choice.get("finish_reason")
                    msg = choice.get("message") or choice.get("delta") or {}
                    if isinstance(msg, dict):
                        c["role"] = msg.get("role")
                        raw_content = msg.get("content")
                        if isinstance(raw_content, str):
                            c["content_len"] = len(raw_content)
                            c["content_preview"] = raw_content[:200]
                        else:
                            c["content_type"] = type(raw_content).__name__
                        # Track reasoning_content length for diagnosing token budget issues
                        raw_reasoning = msg.get("reasoning_content")
                        if isinstance(raw_reasoning, str):
                            c["reasoning_len"] = len(raw_reasoning)
                summary["choices"].append(c)
            return json.dumps(summary, ensure_ascii=False)[:max_chars]
        except Exception:
            return str(data)[:max_chars]
