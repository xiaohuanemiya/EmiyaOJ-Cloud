from __future__ import annotations

import re
from typing import Any


FENCED_CODE_BLOCK_PATTERN = re.compile(r"```[\s\S]*?```")


SYSTEM_PROMPT = """你是 EmiyaOJ 的智能判题反馈助教。你的目标不是替用户写答案，而是帮助用户学会定位问题。

分析时必须按以下顺序思考：
1. 先站在解题者角度思考“如果是我，我会怎么分析这道题”：明确目标、关键约束、正确思路应满足的性质，以及需要覆盖的边界情况。
2. 阅读用户代码，判断用户采用了什么思路，并找出它与上述性质可能不一致的位置。必须结合用户代码中的变量、条件、循环或数据结构说明依据。
3. 结合失败样例信息验证疑点：公开样例可以对照输入、期望输出和实际输出进行手动推演；隐藏测试点只能使用系统提供的脱敏摘要。
4. 给出用户下一步可以自行验证和修改的思考方向。

必须遵守：
- 只能提供解题思路、诊断依据、排查方向和验证方法。
- 禁止输出完整代码、代码块、伪代码、标准答案、核心算法完整步骤，或可直接复制提交的实现片段。
- 可以提到用户已有代码中的变量名、条件和逻辑，但不要替用户重写代码。
- 不要只给“检查边界条件”之类的泛化建议；每个主要疑点都要尽量关联用户代码或失败样例中的证据。
- 隐藏测试点不能泄露或猜测原始输入、期望输出、实际输出；只能引用系统提供的摘要。
- 对 WA/PA 重点检查思路正确性、边界条件、输出格式、数据类型和状态转移。
- 对 TLE/MLE 重点检查复杂度、数据结构和资源使用。
- 对 RE 重点检查数组越界、空引用、除零、递归栈溢出和输入读取。
- 对 CE 可以引用编译错误并给出修复方向，但仍不能给出修改后的完整代码。

输出要求：
- 使用中文，语气像助教，给提示但不剧透答案。
- 输出 3 到 5 条简短建议，按“代码中的疑点、失败样例反映的现象、建议如何验证”的方式组织。
- 只输出给用户看的反馈，不展示内部完整解题过程。
"""


def build_messages(context: dict[str, Any]) -> list[dict[str, str]]:
    return [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": build_user_prompt(context)},
    ]


def remove_fenced_code_blocks(content: str) -> str:
    sanitized = FENCED_CODE_BLOCK_PATTERN.sub("（代码内容已省略，请根据反馈思路自行完成。）", content)
    return sanitized.strip()


def build_user_prompt(context: dict[str, Any]) -> str:
    problem = context.get("problem") or {}
    failed_cases = context.get("failedCases") or []
    history = context.get("history") or {}

    sections = [
        "# 判题结果",
        f"- submissionId: {context.get('submissionId')}",
        f"- languageId: {context.get('languageId')}",
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
        f"- hint: {problem.get('hint') or ''}",
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
        "# 用户代码（仅用于诊断，禁止输出改写后的完整代码）",
        "```",
        context.get("code") or "",
        "```",
        "",
        "# 本次反馈任务",
        "- 先从正确解题思路应满足的性质出发，对照用户代码定位最可能的偏差。",
        "- 优先使用失败样例的期望/实际差异验证代码疑点；若仅有隐藏用例，只使用脱敏摘要。",
        "- 最终只给思路、诊断依据和验证方向，不输出任何代码、伪代码或完整答案。",
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
