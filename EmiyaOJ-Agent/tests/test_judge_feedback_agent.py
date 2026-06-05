from emiyaoj_agent.agents.judge_feedback.agent import JudgeFeedbackAgent
from emiyaoj_agent.agents.judge_feedback.prompts import build_user_prompt
from emiyaoj_agent.core.models import AgentTask
from emiyaoj_agent.llm.base import LLMClientError


class FakeJudgeClient:
    def __init__(self, context):
        self.context = context
        self.feedback = None

    def get_feedback_context(self, submission_id: int):
        assert submission_id == self.context["submissionId"]
        return self.context

    def submit_feedback(self, result):
        self.feedback = result
        return {"id": 1}


class FakeLLM:
    def __init__(self, content="看起来需要检查边界条件。", error=None):
        self.content = content
        self.error = error
        self.messages = None

    def generate(self, messages, options):
        self.messages = messages
        if self.error:
            raise self.error
        return self.content


def context_with_hidden_case():
    return {
        "submissionId": 42,
        "status": 5,
        "statusText": "WRONG_ANSWER",
        "passedCaseCount": 1,
        "totalCaseCount": 2,
        "score": 50,
        "maxTimeUsed": 12,
        "maxMemoryUsed": 128,
        "errorMessage": "Wrong Answer",
        "compileMessage": None,
        "code": "print('hello')",
        "problem": {
            "title": "A+B",
            "description": "计算两个数之和",
            "inputDescription": "两个整数",
            "outputDescription": "一个整数",
            "sampleInput": "1 2",
            "sampleOutput": "3",
            "tags": ["入门"],
        },
        "failedCases": [
            {
                "caseOrder": 2,
                "status": 5,
                "statusText": "WRONG_ANSWER",
                "isSample": 0,
                "score": 0,
                "timeUsed": 5,
                "memoryUsed": 64,
                "errorMessage": "Wrong Answer",
                "inputPreview": "SECRET_INPUT",
                "expectedOutputPreview": "SECRET_EXPECTED",
                "actualOutputPreview": "SECRET_ACTUAL",
                "outputDiffSummary": "Hidden case output differs. firstDifferentLine=1",
            }
        ],
        "history": {
            "totalAttempts": 2,
            "nonAcceptedAttempts": 2,
            "acceptedBefore": False,
            "recentStatuses": [5, 5],
        },
    }


def test_prompt_does_not_include_hidden_case_raw_previews():
    prompt = build_user_prompt(context_with_hidden_case())

    assert "SECRET_INPUT" not in prompt
    assert "SECRET_EXPECTED" not in prompt
    assert "SECRET_ACTUAL" not in prompt
    assert "Hidden case output differs" in prompt


def test_agent_writes_llm_feedback():
    context = context_with_hidden_case()
    judge = FakeJudgeClient(context)
    llm = FakeLLM(content="请重点检查输出格式和边界条件。")
    agent = JudgeFeedbackAgent(judge, llm, model="qwen-plus")

    result = agent.handle(
        AgentTask.model_validate(
            {
                "agentType": "JUDGE_FEEDBACK",
                "taskId": "task-1",
                "traceId": "trace-1",
                "submissionId": 42,
            }
        )
    )

    assert result.status == "SUCCESS"
    assert result.source == "LLM"
    assert judge.feedback.content == "请重点检查输出格式和边界条件。"


def test_agent_writes_static_fallback_when_llm_fails():
    context = context_with_hidden_case()
    judge = FakeJudgeClient(context)
    llm = FakeLLM(error=LLMClientError("timeout"))
    agent = JudgeFeedbackAgent(judge, llm, model="qwen-plus")

    result = agent.handle(
        AgentTask.model_validate(
            {
                "agentType": "JUDGE_FEEDBACK",
                "taskId": "task-1",
                "traceId": "trace-1",
                "submissionId": 42,
            }
        )
    )

    assert result.status == "STATIC_FALLBACK"
    assert result.source == "STATIC_FALLBACK"
    assert "边界" in result.content
    assert judge.feedback.error_message == "timeout"
