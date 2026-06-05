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

    dashscope_api_key: str | None = Field(default=None, alias="JUDGE_FEEDBACK_API_KEY")
    dashscope_api_url: str = Field(
        default="https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
        alias="JUDGE_FEEDBACK_API_URL",
    )
    judge_feedback_model: str = Field(default="qwen-plus", alias="JUDGE_FEEDBACK_MODEL")
    judge_feedback_timeout_seconds: float = Field(default=15.0, alias="JUDGE_FEEDBACK_TIMEOUT_SECONDS")
    judge_feedback_max_tokens: int = Field(default=1200, alias="JUDGE_FEEDBACK_MAX_TOKENS")


def load_settings() -> Settings:
    return Settings()
