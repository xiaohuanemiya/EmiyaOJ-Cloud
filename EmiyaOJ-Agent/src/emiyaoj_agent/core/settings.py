from __future__ import annotations

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    agent_service_name: str = Field(default="emiyaoj-agent", alias="AGENT_SERVICE_NAME")

    rabbitmq_host: str = Field(default="localhost", alias="RABBITMQ_HOST")
    rabbitmq_port: int = Field(default=5672, alias="RABBITMQ_PORT")
    rabbitmq_username: str = Field(default="guest", alias="RABBITMQ_USERNAME")
    rabbitmq_password: str = Field(default="guest", alias="RABBITMQ_PASSWORD")
    rabbitmq_prefetch_count: int = Field(default=5, alias="RABBITMQ_PREFETCH_COUNT")
    rabbitmq_heartbeat_seconds: int = Field(default=300, gt=0, alias="RABBITMQ_HEARTBEAT_SECONDS")

    agent_exchange: str = Field(default="emiyaoj.agent.exchange", alias="AGENT_EXCHANGE")
    agent_dead_letter_exchange: str = Field(default="emiyaoj.agent.dlx", alias="AGENT_DEAD_LETTER_EXCHANGE")
    judge_feedback_queue: str = Field(
        default="emiyaoj.agent.judge-feedback.queue",
        alias="JUDGE_FEEDBACK_QUEUE",
    )
    judge_feedback_dlq: str = Field(
        default="emiyaoj.agent.judge-feedback.dlq",
        alias="JUDGE_FEEDBACK_DLQ",
    )
    judge_feedback_routing_key: str = Field(
        default="agent.judge.feedback",
        alias="JUDGE_FEEDBACK_ROUTING_KEY",
    )
    judge_feedback_dlq_routing_key: str = Field(
        default="agent.judge.feedback.dead",
        alias="JUDGE_FEEDBACK_DLQ_ROUTING_KEY",
    )

    judge_base_url: str = Field(default="http://localhost:9030", alias="JUDGE_BASE_URL")
    judge_internal_token: str = Field(default="emiyaoj-judge-internal", alias="JUDGE_INTERNAL_TOKEN")
    judge_http_timeout_seconds: float = Field(default=10.0, gt=0, alias="JUDGE_HTTP_TIMEOUT_SECONDS")

    deepseek_api_key: str | None = Field(default=None, alias="DEEPSEEK_API_KEY")
    judge_feedback_api_key: str | None = Field(default=None, alias="JUDGE_FEEDBACK_API_KEY")
    chat_api_key: str | None = Field(default=None, alias="CHAT_API_KEY")
    judge_feedback_api_url: str = Field(
        default="https://api.deepseek.com/chat/completions",
        alias="JUDGE_FEEDBACK_API_URL",
    )
    judge_feedback_model: str = Field(default="deepseek-v4-pro", alias="JUDGE_FEEDBACK_MODEL")
    judge_feedback_timeout_seconds: float = Field(
        default=120.0,
        gt=0,
        alias="JUDGE_FEEDBACK_TIMEOUT_SECONDS",
    )
    judge_feedback_max_tokens: int = Field(default=16384, alias="JUDGE_FEEDBACK_MAX_TOKENS")

    @property
    def llm_api_key(self) -> str | None:
        for value in (self.deepseek_api_key, self.judge_feedback_api_key, self.chat_api_key):
            if value is not None and value.strip():
                return value.strip()
        return None


def load_settings() -> Settings:
    return Settings()
