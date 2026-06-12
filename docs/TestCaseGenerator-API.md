# TestCaseGenerator API 详细接口文档

本文档描述 EmiyaOJ-Cloud 中“测试数据生成器”业务接口。该业务用于为每个题目维护一份测试数据生成器描述 `TestCaseGeneratorSpec` 和一份 Python 测试数据生成器脚本 `TestCaseGenerator`，并通过运行脚本自动生成测试用例写入 `test_case` 表。

## 1. 基本说明

### 1.1 服务与路径

| 项目 | 说明 |
| --- | --- |
| 所属服务 | `problem-service` |
| 网关路径 | `/test-case-generator/**` |
| 直连服务端口 | `9020` |
| 数据库 | `emiya_oj_problem` |
| 主表 | `test_case_generator` |
| 生成结果写入表 | `test_case` |

### 1.2 统一响应格式

所有接口统一返回 `ResponseResult<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | Integer | 业务状态码，`200` 表示成功 |
| `message` | String | 响应消息 |
| `data` | Object | 响应数据 |

### 1.3 鉴权说明

接口依赖网关解析 JWT 后注入的请求头：

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌，走网关时由网关校验 |
| `X-User-Id` | 写接口必填 | 当前用户 ID，由网关注入 |
| `X-User-Roles` | 是 | 当前用户权限编码列表，由网关注入，逗号分隔 |

权限复用测试用例管理权限：

| 操作 | 所需权限 |
| --- | --- |
| 查询生成器描述或脚本 | `TESTCASE.LIST` |
| 创建/更新生成器描述 | `TESTCASE.EDIT` |
| 更新生成器脚本 | `TESTCASE.EDIT` |
| 运行生成器并保存测试用例 | `TESTCASE.ADD` |

> 注意：客户端应通过网关访问接口，不应直连 `problem-service` 并自行伪造 `X-User-*` 请求头。

## 2. 核心数据协议

### 2.1 每题唯一生成器

每个题目最多维护一条 `test_case_generator` 记录，由 `problem_id` 唯一约束保证。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long | 生成器记录 ID |
| `problemId` | Long | 题目 ID |
| `spec` | String | 测试数据生成器描述 |
| `generatorCode` | String | Python 生成器脚本 |
| `createBy` | Long | 创建者 ID |
| `updateBy` | Long | 最近更新者 ID |
| `createTime` | LocalDateTime | 创建时间 |
| `updateTime` | LocalDateTime | 更新时间 |

### 2.2 Python 生成器 stdout 协议

Python 脚本必须向标准输出 `stdout` 输出 JSON 数组，数组中每个元素表示一个测试用例。

```json
[
  {
    "input": "1 2\n",
    "output": "3\n",
    "isSample": 0,
    "score": 10,
    "sortOrder": 1
  }
]
```

测试用例字段说明：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `input` | String/null | 否 | `null` | 测试输入数据；缺省或 `null` 表示无标准输入 |
| `output` | String/null | 否 | `null` | 标准输出数据；缺省或 `null` 表示期望无输出 |
| `isSample` | Integer | 否 | `0` | 是否样例：`0` 非样例，`1` 样例 |
| `score` | Integer | 否 | `0` | OI/IOI 分值权重 |
| `sortOrder` | Integer | 否 | 当前题目最大排序值 + 1 | 用例排序 |

约束规则：

| 规则 | 说明 |
| --- | --- |
| JSON 必须是数组 | 非数组或非法 JSON 会返回参数错误 |
| 至少生成 1 条用例 | 空数组会被拒绝 |
| `input`/`output` 可缺省或为 `null` | 判题时按空字符串处理；有内容时应保留真实换行 |
| `isSample` 只能为 `0` 或 `1` | 其他值会被拒绝 |
| `score` 不能为负数 | 负数会被拒绝 |
| 默认最大生成数量 | `test-case-generator.max-generated-cases`，默认 `1000` |

### 2.3 Python 生成器示例

```python
import json

cases = [
    {
        "input": "1 2\n",
        "output": "3\n",
        "isSample": 1,
        "score": 0,
        "sortOrder": 1
    },
    {
        "input": "100 200\n",
        "output": "300\n",
        "isSample": 0,
        "score": 10,
        "sortOrder": 2
    }
]

print(json.dumps(cases, ensure_ascii=False))
```

## 3. 管理端接口

### 3.1 创建测试数据生成器描述

`POST /test-case-generator/{problemId}/spec`

为指定题目创建 `TestCaseGeneratorSpec`。每个题目只能创建一次；如果已存在生成器记录，应调用更新接口。

#### Path 参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `problemId` | Long | 是 | 题目 ID |

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌 |
| `X-User-Id` | 是 | 当前用户 ID |
| `X-User-Roles` | 是 | 需要包含 `TESTCASE.EDIT` |

#### 请求体

```json
{
  "spec": "为 A+B 题目生成边界用例、随机用例和大数用例。生成器 stdout 必须输出测试用例 JSON 数组。"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `spec` | String | 是 | 生成器描述，说明数据范围、边界条件、分组策略和输出要求 |

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "problemId": 1,
    "spec": "为 A+B 题目生成边界用例、随机用例和大数用例。生成器 stdout 必须输出测试用例 JSON 数组。",
    "createBy": 2044312993243619329,
    "updateBy": 2044312993243619329,
    "createTime": "2026-05-28T10:00:00",
    "updateTime": "2026-05-28T10:00:00"
  }
}
```

#### 失败场景

| code | 场景 | message 示例 |
| --- | --- | --- |
| `400` | `spec` 为空 | `Test case generator spec cannot be empty` |
| `400` | 题目已存在生成器 | `Test case generator already exists` |
| `403` | 权限不足 | `Missing permission: TESTCASE.EDIT` |
| `404` | 题目不存在 | `Problem does not exist` |

### 3.2 更新测试数据生成器描述

`PUT /test-case-generator/{problemId}/spec`

更新指定题目的 `TestCaseGeneratorSpec`。

#### Path 参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `problemId` | Long | 是 | 题目 ID |

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌 |
| `X-User-Id` | 是 | 当前用户 ID |
| `X-User-Roles` | 是 | 需要包含 `TESTCASE.EDIT` |

#### 请求体

```json
{
  "spec": "更新后的数据生成要求：包含 2 组样例、10 组随机小数据、5 组大数据。"
}
```

#### 成功响应

响应 `data` 类型：`TestCaseGeneratorSpecVO`，结构同创建接口。

#### 失败场景

| code | 场景 | message 示例 |
| --- | --- | --- |
| `400` | `spec` 为空 | `Test case generator spec cannot be empty` |
| `403` | 权限不足 | `Missing permission: TESTCASE.EDIT` |
| `404` | 题目不存在 | `Problem does not exist` |
| `404` | 生成器不存在 | `Test case generator does not exist` |

### 3.3 查询测试数据生成器描述

`GET /test-case-generator/{problemId}/spec`

只查询生成器描述，不返回 Python 脚本。

#### Path 参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `problemId` | Long | 是 | 题目 ID |

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌 |
| `X-User-Roles` | 是 | 需要包含 `TESTCASE.LIST` |

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "problemId": 1,
    "spec": "为 A+B 题目生成边界用例、随机用例和大数用例。",
    "createBy": 2044312993243619329,
    "updateBy": 2044312993243619329,
    "createTime": "2026-05-28T10:00:00",
    "updateTime": "2026-05-28T10:10:00"
  }
}
```

### 3.4 查询完整测试数据生成器

`GET /test-case-generator/{problemId}`

查询完整生成器记录，包含 `spec` 和 `generatorCode`。该接口会返回隐藏用例生成逻辑，仅供管理端使用。

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌 |
| `X-User-Roles` | 是 | 需要包含 `TESTCASE.LIST` |

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "problemId": 1,
    "spec": "为 A+B 题目生成边界用例、随机用例和大数用例。",
    "generatorCode": "import json\nprint(json.dumps([{\"input\":\"1 2\\n\",\"output\":\"3\\n\"}]))",
    "createBy": 2044312993243619329,
    "updateBy": 2044312993243619329,
    "createTime": "2026-05-28T10:00:00",
    "updateTime": "2026-05-28T10:20:00"
  }
}
```

### 3.5 更新 Python 测试数据生成器脚本

`PUT /test-case-generator/{problemId}`

保存或覆盖指定题目的 Python 生成器脚本。

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌 |
| `X-User-Id` | 是 | 当前用户 ID |
| `X-User-Roles` | 是 | 需要包含 `TESTCASE.EDIT` |

#### 请求体

```json
{
  "generatorCode": "import json\ncases = [{\"input\":\"1 2\\n\",\"output\":\"3\\n\",\"isSample\":1,\"sortOrder\":1}]\nprint(json.dumps(cases, ensure_ascii=False))"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `generatorCode` | String | 是 | Python 生成器脚本 |

#### 成功响应

响应 `data` 类型：`TestCaseGeneratorVO`。

#### 失败场景

| code | 场景 | message 示例 |
| --- | --- | --- |
| `400` | `generatorCode` 为空 | `Test case generator code cannot be empty` |
| `403` | 权限不足 | `Missing permission: TESTCASE.EDIT` |
| `404` | 题目不存在 | `Problem does not exist` |
| `404` | 生成器不存在 | `Test case generator does not exist` |

### 3.6 运行 Python 生成器并保存测试用例

`POST /test-case-generator/{problemId}/run`

调用 Judge Service 内部接口，在 Go-Judge 沙箱中执行 Python 生成器。脚本执行成功且 stdout JSON 校验通过后，将生成结果保存到 `test_case` 表。

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 登录令牌 |
| `X-User-Roles` | 是 | 需要包含 `TESTCASE.ADD` |

#### 请求体

```json
{
  "saveMode": "APPEND"
}
```

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `saveMode` | String | 否 | `APPEND` | 保存策略：`APPEND` 追加，`REPLACE` 替换 |

保存策略说明：

| saveMode | 行为 |
| --- | --- |
| `APPEND` | 保留题目已有测试用例，追加生成的新用例 |
| `REPLACE` | 先逻辑删除该题已有测试用例，再保存生成的新用例 |

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "problemId": 1,
    "saveMode": "APPEND",
    "generatedCount": 2,
    "savedCount": 2,
    "timeUsed": 12,
    "memoryUsed": 10240,
    "testCases": [
      {
        "id": 10,
        "problemId": 1,
        "input": "1 2\n",
        "output": "3\n",
        "isSample": 1,
        "score": 0,
        "sortOrder": 1
      },
      {
        "id": 11,
        "problemId": 1,
        "input": "100 200\n",
        "output": "300\n",
        "isSample": 0,
        "score": 10,
        "sortOrder": 2
      }
    ]
  }
}
```

响应字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `problemId` | Long | 题目 ID |
| `saveMode` | String | 实际采用的保存策略 |
| `generatedCount` | Integer | 生成器 stdout 中解析出的用例数量 |
| `savedCount` | Integer | 成功保存到数据库的用例数量 |
| `timeUsed` | Long | 生成器运行耗时，单位毫秒 |
| `memoryUsed` | Long | 生成器运行内存，单位 KB |
| `testCases` | `List<TestCaseVO>` | 已保存测试用例列表 |

#### 失败场景

| code | 场景 | message 示例 |
| --- | --- | --- |
| `400` | `saveMode` 非法 | `saveMode must be APPEND or REPLACE` |
| `400` | 生成器脚本为空 | `Test case generator code cannot be empty` |
| `400` | 沙箱执行失败 | `Generator run failed: Time Limit Exceeded` |
| `400` | stdout 为空 | `Generator stdout cannot be empty` |
| `400` | stdout 不是 JSON 数组 | `Generator stdout must be a JSON array of test cases` |
| `400` | 生成用例为空数组 | `Generator must produce at least one test case` |
| `400` | 生成数量超过限制 | `Generated test case count exceeds 1000` |
| `400` | 生成用例字段非法 | `Generated test case input cannot be null at index 0` |
| `403` | 权限不足 | `Missing permission: TESTCASE.ADD` |
| `404` | 题目不存在 | `Problem does not exist` |
| `404` | 生成器不存在 | `Test case generator does not exist` |
| `500` | 调用 Judge 失败 | `Failed to run test case generator` |

## 4. Judge 内部接口

该接口由 `problem-service` 通过 Feign 调用，不应暴露给普通客户端。

### 4.1 运行 Python 生成器

`POST /judge/internal/test-case-generator/run`

#### 请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `X-Internal-Token` | 是 | 服务间内部调用令牌 |

#### 请求体

```json
{
  "generatorCode": "import json\nprint(json.dumps([{\"input\":\"1 2\\n\",\"output\":\"3\\n\"}]))"
}
```

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "status": "Accepted",
    "stdout": "[{\"input\":\"1 2\\n\",\"output\":\"3\\n\"}]",
    "stderr": "",
    "errorMessage": null,
    "timeUsed": 12,
    "memoryUsed": 10240
  }
}
```

#### 字段说明

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `success` | Boolean | 是否执行成功，即 Go-Judge 状态是否为 `Accepted` |
| `status` | String | Go-Judge 原始状态 |
| `stdout` | String | 生成器标准输出 |
| `stderr` | String | 生成器标准错误 |
| `errorMessage` | String | 执行失败时的错误信息 |
| `timeUsed` | Long | CPU 时间，单位毫秒 |
| `memoryUsed` | Long | 内存使用，单位 KB |

#### 沙箱资源限制

| 项目 | 默认值 |
| --- | --- |
| Python 命令 | `/usr/bin/python3 generator.py` |
| CPU 时间限制 | 5000 ms |
| 实际时间限制 | 10000 ms |
| 内存限制 | 256 MB |
| 进程数限制 | 10 |
| stdout 最大大小 | 64 MB |
| stderr 最大大小 | 4 MB |

## 5. DTO/VO 定义

### 5.1 `TestCaseGeneratorSpecSaveDTO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `spec` | String | 测试数据生成器描述 |

### 5.2 `TestCaseGeneratorUpdateDTO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `generatorCode` | String | Python 生成器脚本 |

### 5.3 `RunTestCaseGeneratorDTO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `saveMode` | String | `APPEND` 或 `REPLACE`，为空时默认 `APPEND` |

### 5.4 `TestCaseGeneratorSpecVO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `problemId` | Long | 题目 ID |
| `spec` | String | 生成器描述 |
| `createBy` | Long | 创建者 |
| `updateBy` | Long | 更新者 |
| `createTime` | LocalDateTime | 创建时间 |
| `updateTime` | LocalDateTime | 更新时间 |

### 5.5 `TestCaseGeneratorVO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long | 生成器 ID |
| `problemId` | Long | 题目 ID |
| `spec` | String | 生成器描述 |
| `generatorCode` | String | Python 生成器脚本 |
| `createBy` | Long | 创建者 |
| `updateBy` | Long | 更新者 |
| `createTime` | LocalDateTime | 创建时间 |
| `updateTime` | LocalDateTime | 更新时间 |

### 5.6 `RunTestCaseGeneratorVO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `problemId` | Long | 题目 ID |
| `saveMode` | String | 保存策略 |
| `generatedCount` | Integer | 解析出的生成用例数量 |
| `savedCount` | Integer | 成功保存数量 |
| `timeUsed` | Long | 生成器运行耗时，单位毫秒 |
| `memoryUsed` | Long | 生成器运行内存，单位 KB |
| `testCases` | `List<TestCaseVO>` | 保存后的测试用例 |

## 6. 推荐调用流程

1. 管理员创建题目。
2. 调用 `POST /test-case-generator/{problemId}/spec` 创建生成器描述。
3. 其他管理员或出题人调用 `GET /test-case-generator/{problemId}/spec` 读取描述。
4. 根据描述编写 Python 脚本。
5. 调用 `PUT /test-case-generator/{problemId}` 保存脚本。
6. 调用 `POST /test-case-generator/{problemId}/run` 运行脚本并保存测试用例。
7. 调用已有接口 `GET /test-case/problem/{problemId}` 检查生成的测试用例。
8. 用户提交代码时，Judge Service 会按原有流程读取 `test_case` 表进行判题。

## 7. 注意事项

- 生成器脚本必须只依赖 Python 标准库，沙箱内不保证安装第三方库。
- 生成器脚本不要通过网络、文件系统持久化或外部服务生成数据。
- 隐藏用例生成逻辑属于敏感数据，完整生成器查询接口只应提供给管理端。
- `REPLACE` 会删除该题已有测试用例，调用前应确认不会误删人工维护的用例。
- 生成器运行成功但 stdout JSON 校验失败时，不会写入任何测试用例。
- 生成器写入测试用例的事务只覆盖数据库保存阶段，不覆盖沙箱运行阶段。
