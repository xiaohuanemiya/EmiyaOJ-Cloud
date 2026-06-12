from emiyaoj_agent.agents.judge_feedback.agent import JudgeFeedbackAgent
from emiyaoj_agent.agents.judge_feedback.prompts import (
    SYSTEM_PROMPT,
    build_user_prompt,
    remove_fenced_code_blocks,
)
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
        "languageId": 3,
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
            "hint": "注意整数范围",
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


def test_prompt_requires_solution_comparison_and_guidance_only():
    prompt = build_user_prompt(context_with_hidden_case())

    assert "如果是我，我会怎么分析这道题" in SYSTEM_PROMPT
    assert "结合用户代码中的变量、条件、循环或数据结构说明依据" in SYSTEM_PROMPT
    assert "禁止输出完整代码、代码块、伪代码" in SYSTEM_PROMPT
    assert "优先使用失败样例的期望/实际差异验证代码疑点" in prompt
    assert "最终只给思路、诊断依据和验证方向" in prompt
    assert "- languageId: 3" in prompt
    assert "- hint: 注意整数范围" in prompt


def test_prompt_includes_public_failed_case_previews_for_reasoning():
    context = context_with_hidden_case()
    context["failedCases"] = [
        {
            "caseOrder": 1,
            "status": 5,
            "statusText": "WRONG_ANSWER",
            "isSample": 1,
            "score": 0,
            "timeUsed": 5,
            "memoryUsed": 64,
            "errorMessage": "Wrong Answer",
            "inputPreview": "1 2",
            "expectedOutputPreview": "3",
            "actualOutputPreview": "hello",
            "outputDiffSummary": "Sample case output differs. firstDifferentLine=1",
        }
    ]

    prompt = build_user_prompt(context)

    assert "sampleInputPreview: 1 2" in prompt
    assert "sampleExpectedOutputPreview: 3" in prompt
    assert "sampleActualOutputPreview: hello" in prompt


def test_removes_fenced_code_blocks_from_feedback():
    feedback = remove_fenced_code_blocks(
        "先检查累加变量的含义。\n```java\nclass Main {}\n```\n再用样例逐步验证。"
    )

    assert "先检查累加变量的含义。" in feedback
    assert "再用样例逐步验证。" in feedback
    assert "class Main" not in feedback
    assert "```" not in feedback


def test_agent_writes_llm_feedback():
    context = context_with_hidden_case()
    judge = FakeJudgeClient(context)
    llm = FakeLLM(content="请重点检查输出格式和边界条件。")
    agent = JudgeFeedbackAgent(judge, llm, model="deepseek-v4-pro")

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


def test_agent_writes_no_output_when_llm_fails():
    context = context_with_hidden_case()
    judge = FakeJudgeClient(context)
    llm = FakeLLM(error=LLMClientError("timeout"))
    agent = JudgeFeedbackAgent(judge, llm, model="deepseek-v4-pro")

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

    assert result.status == "NO_OUTPUT"
    assert result.source == "LLM"
    assert result.content is None
    assert judge.feedback.content is None
    assert "content" not in result.to_callback_payload()
    assert judge.feedback.error_message == "timeout"


def test_agent_writes_no_output_when_llm_returns_empty_content():
    context = context_with_hidden_case()
    judge = FakeJudgeClient(context)
    llm = FakeLLM(content="")
    agent = JudgeFeedbackAgent(judge, llm, model="deepseek-v4-pro")

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

    assert result.status == "NO_OUTPUT"
    assert result.content is None
    assert judge.feedback.content is None
