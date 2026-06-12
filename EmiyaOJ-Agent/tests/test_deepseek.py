import pytest

from emiyaoj_agent.core.models import LLMOptions
from emiyaoj_agent.llm.base import LLMClientError
from emiyaoj_agent.llm.deepseek import DeepSeekClient


def test_extracts_openai_compatible_message_content():
    content, error_reason = DeepSeekClient._extract_content(
        {"choices": [{"message": {"role": "assistant", "content": "  feedback  "}}]}
    )

    assert content == "feedback"
    assert error_reason is None


def test_extract_content_returns_error_reason_for_empty_choices():
    content, error_reason = DeepSeekClient._extract_content({"choices": []})

    assert content is None
    assert error_reason == "empty_choices"


def test_extract_content_returns_finish_reason_for_content_filter():
    content, error_reason = DeepSeekClient._extract_content(
        {
            "choices": [
                {
                    "finish_reason": "content_filter",
                    "message": {"role": "assistant", "content": ""},
                }
            ]
        }
    )

    assert content is None
    assert error_reason == "content_filter"


def test_extract_content_returns_finish_reason_for_length():
    content, error_reason = DeepSeekClient._extract_content(
        {
            "choices": [
                {
                    "finish_reason": "length",
                    "message": {"role": "assistant", "content": None},
                }
            ]
        }
    )

    assert content is None
    assert error_reason == "length"


def test_extract_content_handles_delta_format():
    content, error_reason = DeepSeekClient._extract_content(
        {"choices": [{"delta": {"content": "  hello from delta  "}}]}
    )

    assert content == "hello from delta"
    assert error_reason is None


def test_extract_content_falls_back_to_reasoning_content():
    """When content is empty but reasoning_content exists, use it as fallback."""
    content, error_reason = DeepSeekClient._extract_content(
        {
            "choices": [
                {
                    "finish_reason": "length",
                    "message": {
                        "role": "assistant",
                        "content": "",
                        "reasoning_content": "  推理过程：这道题应该用动态规划...  ",
                    },
                }
            ]
        }
    )

    assert content == "推理过程：这道题应该用动态规划..."
    assert error_reason is None


def test_extract_content_returns_none_when_both_content_and_reasoning_empty():
    """When both content and reasoning_content are empty, return None."""
    content, error_reason = DeepSeekClient._extract_content(
        {
            "choices": [
                {
                    "finish_reason": "length",
                    "message": {
                        "role": "assistant",
                        "content": "",
                        "reasoning_content": "",
                    },
                }
            ]
        }
    )

    assert content is None
    assert error_reason == "length"


def test_missing_api_key_fails_before_request():
    client = DeepSeekClient(None, "https://api.deepseek.com/chat/completions")

    with pytest.raises(LLMClientError, match="DeepSeek API key is not configured"):
        client.generate(
            [{"role": "user", "content": "hello"}],
            LLMOptions(model="deepseek-v4-pro"),
        )


def test_uses_long_response_read_timeout_and_short_connection_timeouts(monkeypatch):
    captured = {}

    class FakeResponse:
        def raise_for_status(self):
            return None

        def json(self):
            return {"choices": [{"message": {"content": "feedback"}}]}

    class FakeClient:
        def __init__(self, *, timeout):
            captured["timeout"] = timeout

        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            return None

        def post(self, url, *, json, headers):
            return FakeResponse()

    monkeypatch.setattr("emiyaoj_agent.llm.deepseek.httpx.Client", FakeClient)

    client = DeepSeekClient("key", "https://api.deepseek.com/chat/completions")
    result = client.generate(
        [{"role": "user", "content": "hello"}],
        LLMOptions(model="deepseek-v4-pro", timeout_seconds=120),
    )

    assert result == "feedback"
    assert captured["timeout"].read == 120
    assert captured["timeout"].connect == 10
    assert captured["timeout"].write == 30
    assert captured["timeout"].pool == 10
