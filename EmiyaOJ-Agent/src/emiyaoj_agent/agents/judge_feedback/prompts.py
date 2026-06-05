from __future__ import annotations

from typing import Any


SYSTEM_PROMPT = """你是 EmiyaOJ 的智能判题反馈助手。
你的任务是根据判题结果、题目信息、用户代码和脱敏失败用例，给出引导式调试建议。

必须遵守：
- 不直接给出完整代码、标准答案或核心算法完整实现。
- 优先帮助用户定位排查方向，而不是替用户改代码。
- 隐藏测试点不能泄露原始输入、期望输出或实际输出；只能引用系统提供的摘要。
- 对 WA/PA 关注边界条件、输出格式、数据类型、状态转移或贪心/搜索/DP 逻辑。
- 对 TLE/MLE 关注复杂度、数据结构和资源使用。
- 对 RE 关注数组越界、空引用、除零、递归栈溢出和输入读取。
- 对 CE 可以引用编译错误并给出修复方向。

输出要求：
- 使用中文。
- 结构清晰，控制在 3 到 5 条短建议内。
- 语气像助教，给提示，不剧透答案。
"""


def build_messages(context: dict[str, Any]) -> list[dict[str, str]]:
    return [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": build_user_prompt(context)},
    ]


def build_user_prompt(context: dict[str, Any]) -> str:
    problem = context.get("problem") or {}
    failed_cases = context.get("failedCases") or []
    history = context.get("history") or {}

    sections = [
        "# 判题结果",
        f"- submissionId: {context.get('submissionId')}",
        f"- status: {context.get('statusText')} ({context.get('status')})",
        f"- passed: {context.get('passedCaseCount')}/{context.get('totalCaseCount')}",
        f"- score: {context.get('score')}",
        f"- maxTimeUsed(ms): {context.get('maxTimeUsed')}",
        f"- maxMemoryUsed(KB): {context.get('maxMemoryUsed')}",
        f"- errorMessage: {context.get('errorMessage') or ''}",
        f"- compileMessage: {context.get('compileMessage') or ''}",
        "",
        "# 题目信息",
        f"- title: {problem.get('title') or ''}",
        f"- difficulty: {problem.get('difficultyDesc') or ''}",
        f"- timeLimit(ms): {problem.get('timeLimit')}",
        f"- memoryLimit(MB): {problem.get('memoryLimit')}",
        f"- tags: {', '.join(problem.get('tags') or [])}",
        f"- description: {problem.get('description') or ''}",
        f"- inputDescription: {problem.get('inputDescription') or ''}",
        f"- outputDescription: {problem.get('outputDescription') or ''}",
        f"- sampleInput: {problem.get('sampleInput') or ''}",
        f"- sampleOutput: {problem.get('sampleOutput') or ''}",
        "",
        "# 用户本题历史",
        f"- totalAttempts: {history.get('totalAttempts')}",
        f"- nonAcceptedAttempts: {history.get('nonAcceptedAttempts')}",
        f"- acceptedBefore: {history.get('acceptedBefore')}",
        f"- recentStatuses: {history.get('recentStatuses')}",
        "",
        "# 失败用例提示",
        *[_format_case_hint(case) for case in failed_cases],
        "",
        "# 用户代码",
        "```",
        context.get("code") or "",
        "```",
    ]
    return "\n".join(sections)


def _format_case_hint(case: dict[str, Any]) -> str:
    is_sample = case.get("isSample") == 1
    base = [
        f"- caseOrder: {case.get('caseOrder')}",
        f"  status: {case.get('statusText')} ({case.get('status')})",
        f"  score: {case.get('score')}",
        f"  timeUsed(ms): {case.get('timeUsed')}",
        f"  memoryUsed(KB): {case.get('memoryUsed')}",
        f"  errorMessage: {case.get('errorMessage') or ''}",
        f"  outputDiffSummary: {case.get('outputDiffSummary') or ''}",
    ]
    if is_sample:
        base.extend(
            [
                f"  sampleInputPreview: {case.get('inputPreview') or ''}",
                f"  sampleExpectedOutputPreview: {case.get('expectedOutputPreview') or ''}",
                f"  sampleActualOutputPreview: {case.get('actualOutputPreview') or ''}",
            ]
        )
    else:
        base.append("  hiddenCase: true; raw input/output intentionally unavailable")
    return "\n".join(base)


def static_fallback(context: dict[str, Any]) -> str:
    status = context.get("status")
    status_text = context.get("statusText") or "UNKNOWN"
    if status == 3:
        return "编译没有通过。请先查看编译信息，重点检查语法、类名/文件名、头文件或库函数使用是否正确。"
    if status in (5, 10):
        return (
            f"判题结果为 {status_text}。建议先用样例和几个边界数据手动推演，"
            "重点检查输出格式、边界条件和数据类型范围。"
        )
    if status == 6:
        return "程序运行超时。请结合数据范围估算复杂度，思考是否需要更高效的数据结构或算法。"
    if status == 7:
        return "程序内存超限。请检查数组规模、缓存的中间状态以及递归/容器是否可能快速膨胀。"
    if status == 8:
        return "程序运行时错误。请重点检查数组越界、空引用、除零、递归栈溢出和输入读取数量。"
    return "当前提交未通过。AI 反馈暂时不可用，请先根据判题状态、错误信息和样例进行定位。"
