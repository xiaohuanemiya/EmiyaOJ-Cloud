# 智能判题反馈前端接口变更文档

本文档说明智能判题反馈功能对前端可见的接口与字段变更。

## 1. 变更概览

| 变更类型 | 接口 | 说明 |
| --- | --- | --- |
| 字段新增 | `GET /submission/{id}` | 提交详情新增 `feedback`；测试用例明细新增脱敏提示字段 |
| 接口新增 | `GET /submission/{id}/feedback` | 单独查询某次提交的智能反馈，适合前端轮询 |

智能反馈仅针对非 AC 提交异步生成。判题完成与反馈生成之间存在短暂延迟，不应阻塞判题结果展示。

## 2. 推荐前端流程

1. 调用 `POST /judge/submit` 提交代码，保存返回的提交 ID。
2. 轮询 `GET /submission/{id}`，直到判题状态不再是 `0-PENDING` 或 `1-JUDGING`。
3. 如果判题状态为 `2-AC`，停止轮询，不展示智能反馈区域。
4. 如果判题状态为非 AC：
   - 若 `feedback.status` 为 `SUCCESS`，展示 `content`。
   - 若 `feedback` 为 `null`，表示 Agent 尚未输出或没有输出；可继续轮询 `GET /submission/{id}/feedback`。
5. 建议每 2 秒轮询一次，最长轮询 20 秒；超时后展示“智能反馈生成中，请稍后刷新”。

## 3. 提交详情接口变更

### GET `/submission/{id}`

查询提交详情。原有字段保持不变，新增：

- `feedback`：智能判题反馈。仅 Agent 成功生成非空正文时返回对象，否则为 `null`。
- `caseResults[*]`：新增样例预览和输出差异摘要字段。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1900000000000000000,
    "problemId": 1,
    "userId": 1001,
    "languageId": 1,
    "status": 5,
    "passedCaseCount": 1,
    "totalCaseCount": 3,
    "score": 33,
    "maxTimeUsed": 18,
    "maxMemoryUsed": 10240,
    "errorMessage": "Wrong Answer",
    "compileMessage": null,
    "createTime": "2026-06-12T10:00:00",
    "finishTime": "2026-06-12T10:00:03",
    "caseResults": [
      {
        "id": 1,
        "submissionId": 1900000000000000000,
        "testCaseId": 11,
        "caseOrder": 1,
        "status": 5,
        "score": 0,
        "timeUsed": 12,
        "memoryUsed": 9216,
        "errorMessage": null,
        "isSample": 1,
        "inputPreview": "1 2",
        "expectedOutputPreview": "3",
        "actualOutputPreview": "4",
        "outputDiffSummary": "Sample case output differs. expectedLength=1, actualLength=1, expectedLines=1, actualLines=1, firstDifferentLine=1",
        "createTime": "2026-06-12T10:00:02"
      },
      {
        "id": 2,
        "submissionId": 1900000000000000000,
        "testCaseId": 12,
        "caseOrder": 2,
        "status": 5,
        "score": 0,
        "timeUsed": 15,
        "memoryUsed": 9216,
        "errorMessage": null,
        "isSample": 0,
        "inputPreview": null,
        "expectedOutputPreview": null,
        "actualOutputPreview": null,
        "outputDiffSummary": "Hidden case output differs. expectedLength=8, actualLength=7, expectedLines=1, actualLines=1, firstDifferentLine=1",
        "createTime": "2026-06-12T10:00:02"
      }
    ],
    "feedback": {
      "id": 1,
      "submissionId": 1900000000000000000,
      "status": "SUCCESS",
      "content": "建议重点检查边界条件与输出格式，并使用题目样例逐步推演。",
      "source": "LLM",
      "model": "deepseek-v4-pro",
      "agentType": "JUDGE_FEEDBACK",
      "traceId": "5bd5ac12-50cb-44a5-9633-938f1cb62834",
      "errorMessage": null,
      "createTime": "2026-06-12T10:00:05",
      "updateTime": "2026-06-12T10:00:05"
    }
  }
}
```

### 新增 `caseResults` 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `isSample` | Integer | `1` 表示公开样例，`0` 表示隐藏测试点 |
| `inputPreview` | String / null | 样例输入预览；隐藏测试点固定为 `null` |
| `expectedOutputPreview` | String / null | 样例期望输出预览；隐藏测试点固定为 `null` |
| `actualOutputPreview` | String / null | 样例实际输出预览；隐藏测试点固定为 `null` |
| `outputDiffSummary` | String / null | 输出差异摘要，不包含隐藏测试点原始内容 |

前端必须根据 `isSample` 决定是否展示输入输出预览。不要为隐藏测试点提供“查看原始数据”入口。

## 4. 智能反馈查询接口

### GET `/submission/{id}/feedback`

单独查询某次提交的智能反馈。

Path 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | Long | 是 | 提交 ID |

反馈已生成：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "submissionId": 1900000000000000000,
    "status": "SUCCESS",
    "content": "建议重点检查边界条件与输出格式，并使用题目样例逐步推演。",
    "source": "LLM",
    "model": "deepseek-v4-pro",
    "agentType": "JUDGE_FEEDBACK",
    "traceId": "5bd5ac12-50cb-44a5-9633-938f1cb62834",
    "errorMessage": null,
    "createTime": "2026-06-12T10:00:05",
    "updateTime": "2026-06-12T10:00:05"
  }
}
```

生成中、Agent 失败或 Agent 没有输出时返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

提交不存在时：

```json
{
  "code": 404,
  "message": "Submission does not exist",
  "data": null
}
```

注意：当前统一响应模型通过响应体中的 `code` 表示业务结果。前端应判断 `response.data.code`，不能只依赖 HTTP 状态码。

## 5. `JudgeFeedbackVO` 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long | 反馈记录 ID |
| `submissionId` | Long | 对应提交 ID |
| `status` | String | 反馈生成状态 |
| `content` | String / null | 面向用户展示的反馈正文 |
| `source` | String / null | 反馈来源 |
| `model` | String / null | 使用的模型名称 |
| `agentType` | String | 当前固定为 `JUDGE_FEEDBACK` |
| `traceId` | String / null | Agent 链路追踪 ID，建议仅用于问题排查 |
| `errorMessage` | String / null | Agent 内部错误摘要，不建议直接展示给普通用户 |
| `createTime` | LocalDateTime | 创建时间 |
| `updateTime` | LocalDateTime | 更新时间 |

### 反馈状态

| `status` | `source` | 前端处理建议 |
| --- | --- | --- |
| `PENDING` | `AGENT` | 内部状态，对外反馈返回 `null` |
| `SUCCESS` | `LLM` | 正常展示 `content`，可标记为“AI 智能反馈” |
| `NO_OUTPUT` | `LLM` / `AGENT` | 内部状态，对外反馈返回 `null` |

前端应对未知 `status` 使用兼容性兜底：存在 `content` 时展示正文，否则显示“智能反馈暂时不可用”。

## 6. 展示建议

- 仅在判题状态为非 AC 时显示“智能反馈”区域。
- `content` 当前为普通文本，建议保留换行展示；不要按可信 HTML 直接渲染。
- `errorMessage`、`traceId` 和 `model` 默认不面向普通用户展示，可放入管理员调试信息。
- Agent 没有生成正文时接口返回 `null`，前端不应展示内部错误信息。
- 智能反馈为提示性质，建议附带简短说明：“反馈由 AI 生成，仅供调试参考”。

## 7. TypeScript 类型参考

```ts
export interface JudgeFeedback {
  id: number | string
  submissionId: number | string
  status: 'SUCCESS' | string
  content: string | null
  source: 'LLM' | string | null
  model: string | null
  agentType: 'JUDGE_FEEDBACK' | string
  traceId: string | null
  errorMessage: string | null
  createTime: string
  updateTime: string
}

export interface SubmissionCaseResult {
  id: number | string
  submissionId: number | string
  testCaseId: number | string
  caseOrder: number
  status: number
  score: number
  timeUsed: number
  memoryUsed: number
  errorMessage: string | null
  isSample: 0 | 1
  inputPreview: string | null
  expectedOutputPreview: string | null
  actualOutputPreview: string | null
  outputDiffSummary: string | null
  createTime: string
}

export interface SubmissionDetail {
  // 原 SubmissionVO 字段保持不变
  caseResults: SubmissionCaseResult[]
  feedback: JudgeFeedback | null
}
```

JavaScript 的 `number` 无法安全表示所有 64 位 Long。若网关或后端将 ID 序列化为字符串，前端应始终使用 `string` 保存提交 ID，不要执行数值运算。
