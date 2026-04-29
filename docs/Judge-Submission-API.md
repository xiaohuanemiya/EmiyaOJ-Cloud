# Judge Submission API

本文档记录判题提交与查询接口。统一响应体为 `ResponseResult<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 判题状态码

| Code | Name | Description |
| --- | --- | --- |
| 0 | PENDING | 待判题 |
| 1 | JUDGING | 判题中 |
| 2 | AC | Accepted，通过 |
| 3 | CE | Compile Error，编译错误 |
| 4 | SE | System Error，系统错误 |
| 5 | WA | Wrong Answer，答案错误 |
| 6 | TLE | Time Limit Exceeded，时间超限 |
| 7 | MLE | Memory Limit Exceeded，内存超限 |
| 8 | RE | Runtime Error，运行错误 |
| 9 | OLE | Output Limit Exceeded，输出超限 |
| 10 | PA | Partial Accepted，部分通过 |

## POST `/judge/submit`

提交代码并创建异步判题任务。

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | 当前登录用户 ID |

Request body:

```json
{
  "problemId": 1,
  "languageId": 1,
  "code": "public class Main { public static void main(String[] args) {} }"
}
```

Response `data`: `SubmissionVO`

```json
{
  "id": 1900000000000000000,
  "problemId": 1,
  "userId": 1001,
  "languageId": 1,
  "status": 0,
  "passedCaseCount": 0,
  "totalCaseCount": 0,
  "score": 0,
  "maxTimeUsed": 0,
  "maxMemoryUsed": 0,
  "errorMessage": null,
  "compileMessage": null,
  "createTime": "2026-04-29T10:00:00",
  "finishTime": null
}
```

## GET `/submission/{id}`

查询提交详情，包含汇总结果与已运行测试用例明细。隐藏测试用例的输入、标准答案和实际输出不会返回。

Response `data`: `SubmissionDetailVO`

```json
{
  "id": 1900000000000000000,
  "problemId": 1,
  "userId": 1001,
  "languageId": 1,
  "status": 10,
  "passedCaseCount": 2,
  "totalCaseCount": 3,
  "score": 67,
  "maxTimeUsed": 18,
  "maxMemoryUsed": 10240,
  "errorMessage": "Partial Accepted (2/3)",
  "compileMessage": null,
  "createTime": "2026-04-29T10:00:00",
  "finishTime": "2026-04-29T10:00:03",
  "caseResults": [
    {
      "id": 1,
      "submissionId": 1900000000000000000,
      "testCaseId": 11,
      "caseOrder": 1,
      "status": 2,
      "score": 1,
      "timeUsed": 12,
      "memoryUsed": 9216,
      "errorMessage": null,
      "createTime": "2026-04-29T10:00:02"
    }
  ]
}
```

## GET `/submission/page`

分页查询提交记录，只返回汇总结果。

Query:

| Name | Required | Description |
| --- | --- | --- |
| `pageNum` | no | 页码 |
| `pageSize` | no | 每页条数 |
| `problemId` | no | 按题目过滤 |
| `userId` | no | 按用户过滤 |

Response `data`: `PageVO<SubmissionVO>`

```json
{
  "total": 1,
  "list": [
    {
      "id": 1900000000000000000,
      "problemId": 1,
      "userId": 1001,
      "languageId": 1,
      "status": 2,
      "passedCaseCount": 10,
      "totalCaseCount": 10,
      "score": 100,
      "maxTimeUsed": 20,
      "maxMemoryUsed": 10240,
      "errorMessage": null,
      "compileMessage": null,
      "createTime": "2026-04-29T10:00:00",
      "finishTime": "2026-04-29T10:00:03"
    }
  ],
  "pageNum": 1,
  "pageSize": 10
}
```

## GET `/submission/my`

查询当前用户提交记录，只返回汇总结果。

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | 当前登录用户 ID |

Query:

| Name | Required | Description |
| --- | --- | --- |
| `pageNum` | no | 页码 |
| `pageSize` | no | 每页条数 |
| `problemId` | no | 按题目过滤 |

Response `data`: `PageVO<SubmissionVO>`，结构同 `/submission/page`。

## DTO 字段

`SubmissionVO`:

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long | 提交 ID |
| `problemId` | Long | 题目 ID |
| `userId` | Long | 用户 ID |
| `languageId` | Long | 语言 ID |
| `status` | Integer | 判题状态码 |
| `passedCaseCount` | Integer | 通过测试用例数量 |
| `totalCaseCount` | Integer | 测试用例总数 |
| `score` | Integer | 得分，0-100 |
| `maxTimeUsed` | Long | 最高运行时间，毫秒 |
| `maxMemoryUsed` | Long | 最高运行内存，KB |
| `errorMessage` | String | 错误信息 |
| `compileMessage` | String | 编译错误信息 |
| `createTime` | LocalDateTime | 提交时间 |
| `finishTime` | LocalDateTime | 判题完成时间 |

`SubmissionDetailVO`:

| Field | Type | Description |
| --- | --- | --- |
| `caseResults` | List<SubmissionCaseResultVO> | 已运行测试用例明细 |

`SubmissionCaseResultVO`:

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long | 明细结果 ID |
| `submissionId` | Long | 提交 ID |
| `testCaseId` | Long | 测试用例 ID |
| `caseOrder` | Integer | 执行顺序 |
| `status` | Integer | 该用例判题状态 |
| `score` | Integer | 该用例获得分值 |
| `timeUsed` | Long | 运行时间，毫秒 |
| `memoryUsed` | Long | 运行内存，KB |
| `errorMessage` | String | 错误信息 |
| `createTime` | LocalDateTime | 创建时间 |
