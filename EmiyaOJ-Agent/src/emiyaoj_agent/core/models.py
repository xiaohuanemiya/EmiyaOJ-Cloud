from __future__ import annotations

from datetime import datetime
from typing import Any

from pydantic import BaseModel, Field


class AgentTask(BaseModel):
    agent_type: str = Field(alias="agentType")
    task_id: str = Field(alias="taskId")
    trace_id: str | None = Field(default=None, alias="traceId")
    submission_id: int | None = Field(default=None, alias="submissionId")
    problem_id: int | None = Field(default=None, alias="problemId")
    user_id: int | None = Field(default=None, alias="userId")
    status: int | None = None
    created_at: datetime | None = Field(default=None, alias="createdAt")


class AgentResult(BaseModel):
    submission_id: int = Field(alias="submissionId")
    agent_type: str = Field(alias="agentType")
    status: str
    content: str
    source: str
    model: str | None = None
    trace_id: str | None = Field(default=None, alias="traceId")
    error_message: str | None = Field(default=None, alias="errorMessage")

    def to_callback_payload(self) -> dict[str, Any]:
        return self.model_dump(by_alias=True, exclude_none=True)


class LLMOptions(BaseModel):
    model: str
    temperature: float = 0.3
    max_tokens: int = 1200
    timeout_seconds: float = 15.0
