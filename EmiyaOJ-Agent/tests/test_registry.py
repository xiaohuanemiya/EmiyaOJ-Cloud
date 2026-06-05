from emiyaoj_agent.core.models import AgentResult, AgentTask
from emiyaoj_agent.core.registry import AgentRegistry


class DummyAgent:
    agent_type = "DUMMY"

    def handle(self, task: AgentTask) -> AgentResult:
        return AgentResult(
            submissionId=task.submission_id or 1,
            agentType=self.agent_type,
            status="SUCCESS",
            content="ok",
            source="TEST",
        )


def test_registry_routes_by_agent_type():
    registry = AgentRegistry()
    registry.register(DummyAgent())

    result = registry.handle(
        AgentTask.model_validate(
            {
                "agentType": "DUMMY",
                "taskId": "task-1",
                "submissionId": 123,
            }
        )
    )

    assert result.submission_id == 123
    assert result.agent_type == "DUMMY"
    assert registry.agent_types == ("DUMMY",)
