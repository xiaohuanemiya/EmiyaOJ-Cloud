# EmiyaOJ — 题目服务接口文档

> **版本**：v1.0  
> **服务名**：`problem-service`  
> **基础路径**：`http://{gateway-host}/problem-service`  
> **认证方式**：网关通过 JWT 鉴权，并在请求头中注入 `X-User-Id`（当前登录用户 ID）。

---

## 目录

1. [题目管理 `/problem`](#1-题目管理)
2. [测试用例管理 `/test-case`](#2-测试用例管理)
3. [编程语言管理 `/language`](#3-编程语言管理)
4. [通用响应格式](#4-通用响应格式)
5. [数据对象说明](#5-数据对象说明)

---

## 4. 通用响应格式

所有接口统一返回以下 JSON 结构：

```json
{
  "code":    200,
  "message": "操作成功",
  "data":    { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 状态码：200=成功，400=参数错误，404=不存在，500=服务异常 |
| `message` | string | 提示信息 |
| `data` | any | 响应数据，失败时为 null |

---

## 1. 题目管理

### 1.1 分页查询题目列表

- **GET** `/problem/list`

**Query 参数**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `pageNum` | int | 否 | 1 | 当前页码 |
| `pageSize` | int | 否 | 10 | 每页条数 |
| `title` | string | 否 | — | 标题关键字（模糊搜索） |
| `difficulty` | int | 否 | — | 难度：1-简单，2-中等，3-困难 |
| `tagId` | long | 否 | — | 标签 ID |
| `status` | int | 否 | — | 状态：0-隐藏，1-公开 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pages": 10,
    "list": [
      {
        "id": 1,
        "title": "两数之和",
        "difficulty": 1,
        "difficultyDesc": "简单",
        "status": 1,
        "acceptCount": 500,
        "submitCount": 1000,
        "tags": ["数组", "哈希表"]
      }
    ]
  }
}
```

---

### 1.2 查询题目详情

- **GET** `/problem/{id}`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 题目 ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "两数之和",
    "description": "给定整数数组 nums 和整数 target...",
    "inputDescription": "第一行输入 n 和 target",
    "outputDescription": "输出两个数的下标",
    "sampleInput": "4 9\n2 7 11 15",
    "sampleOutput": "0 1",
    "hint": "可使用哈希表优化到 O(n)",
    "difficulty": 1,
    "difficultyDesc": "简单",
    "timeLimit": 1000,
    "memoryLimit": 256,
    "stackLimit": 128,
    "source": "LeetCode",
    "status": 1,
    "acceptCount": 500,
    "submitCount": 1000,
    "tags": ["数组", "哈希表"],
    "createTime": "2024-01-01T00:00:00"
  }
}
```

---

### 1.3 新增题目

- **POST** `/problem`
- **Headers**：`X-User-Id: {userId}`

**Request Body** (`ProblemSaveDTO`)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | string | ✅ | 题目标题 |
| `description` | string | ✅ | 题目描述（支持 Markdown） |
| `inputDescription` | string | 否 | 输入描述 |
| `outputDescription` | string | 否 | 输出描述 |
| `sampleInput` | string | 否 | 样例输入 |
| `sampleOutput` | string | 否 | 样例输出 |
| `hint` | string | 否 | 提示 |
| `difficulty` | int | ✅ | 难度：1/2/3 |
| `timeLimit` | int | ✅ | CPU 时间限制（毫秒） |
| `memoryLimit` | int | ✅ | 内存限制（MB） |
| `stackLimit` | int | 否 | 栈内存限制（MB），默认 128 |
| `source` | string | 否 | 题目来源 |
| `status` | int | 否 | 状态：0-隐藏，1-公开，默认 1 |
| `tagIds` | long[] | 否 | 关联标签 ID 列表 |

---

### 1.4 更新题目

- **PUT** `/problem`
- **Headers**：`X-User-Id: {userId}`
- **Request Body**：同 1.3，`id` 字段必填

---

### 1.5 删除题目

- **DELETE** `/problem/{id}`
- **Headers**：`X-User-Id: {userId}`

> 逻辑删除，不物理删除数据库记录。

---

## 2. 测试用例管理

### 2.1 根据题目 ID 查询测试用例列表

> 此接口主要供**判题服务 Feign 内部调用**，返回该题所有测试用例（含非样例）。

- **GET** `/test-case/problem/{problemId}`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `problemId` | long | 题目 ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "problemId": 10,
      "input": "1 2",
      "output": "3",
      "isSample": 1,
      "score": 10,
      "sortOrder": 0
    }
  ]
}
```

---

### 2.2 根据 ID 查询单个测试用例

- **GET** `/test-case/{id}`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 测试用例 ID |

**响应说明**：成功返回 `TestCaseVO`，不存在时 `code=404`。

---

### 2.3 新增单个测试用例

- **POST** `/test-case`

**Request Body** (`TestCaseSaveDTO`)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `problemId` | long | ✅ | 关联题目 ID |
| `input` | string | ✅ | 输入数据 |
| `output` | string | ✅ | 预期输出 |
| `isSample` | int | 否 | 是否为样例：0-否，1-是，默认 0 |
| `score` | int | 否 | 分值（OI 模式），默认 0 |
| `sortOrder` | int | 否 | 排序权重，默认 0 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 100,
    "problemId": 10,
    "input": "1 2",
    "output": "3",
    "isSample": 1,
    "score": 10,
    "sortOrder": 0
  }
}
```

---

### 2.4 批量新增测试用例

- **POST** `/test-case/batch/{problemId}`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `problemId` | long | 题目 ID（批量数据的 problemId 以此为准） |

**Request Body**：`TestCaseSaveDTO` 数组

```json
[
  { "input": "1 2", "output": "3", "isSample": 0, "score": 10, "sortOrder": 1 },
  { "input": "3 4", "output": "7", "isSample": 0, "score": 10, "sortOrder": 2 }
]
```

**响应**：新增后的 `TestCaseVO` 列表。

---

### 2.5 更新测试用例

- **PUT** `/test-case`

**Request Body** (`TestCaseSaveDTO`)：`id` 必填，其他字段选填（`null` 字段不更新）。

---

### 2.6 删除单个测试用例

- **DELETE** `/test-case/{id}`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 测试用例 ID |

> 逻辑删除（`deleted=1`）。

---

### 2.7 批量删除测试用例

- **DELETE** `/test-case/batch`

**Request Body**：Long 数组

```json
[1, 2, 3]
```

---

### 2.8 删除题目下所有测试用例

- **DELETE** `/test-case/problem/{problemId}`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `problemId` | long | 题目 ID |

> 常用于题目删除时级联清理测试用例。

---

## 3. 编程语言管理

### 3.1 查询启用的编程语言列表（前台）

- **GET** `/language/list`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "Java",
      "version": "17",
      "compileCommand": "javac -encoding UTF-8 {src}",
      "executeCommand": "java -cp {exe} Main",
      "sourceFileExt": "java",
      "executableExt": "class",
      "isCompiled": 1,
      "timeLimitMultiplier": 2.0,
      "memoryLimitMultiplier": 1.5,
      "status": 1
    }
  ]
}
```

---

### 3.2 管理端查询全部编程语言（含禁用）

- **GET** `/language/admin/list`

**响应**：同 3.1，但包含 `status=0` 的禁用语言。

---

### 3.3 根据 ID 查询语言详情（供 Feign 调用）

- **GET** `/language/{id}`

> 只返回 `status=1` 的启用语言，禁用时返回 `code=404`。

---

### 3.4 管理端根据 ID 查询语言详情（不过滤状态）

- **GET** `/language/admin/{id}`

---

### 3.5 新增编程语言

- **POST** `/language`

**Request Body** (`LanguageSaveDTO`)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | ✅ | 语言名称，如 Java、C++ |
| `version` | string | 否 | 版本号，如 17、14 |
| `compileCommand` | string | 否 | 编译命令（`{src}` 占位源文件，`{out}` 占位输出文件） |
| `executeCommand` | string | 否 | 执行命令（`{exe}` 占位可执行文件） |
| `sourceFileExt` | string | 否 | 源文件扩展名，如 java、cpp |
| `executableExt` | string | 否 | 编译产物扩展名 |
| `isCompiled` | int | 否 | 是否需要编译：0-否，1-是 |
| `timeLimitMultiplier` | decimal | 否 | 时间限制乘数，默认 1.0 |
| `memoryLimitMultiplier` | decimal | 否 | 内存限制乘数，默认 1.0 |
| `status` | int | 否 | 状态：0-禁用，1-启用，默认 1 |

> ⚠️ 同名同版本的语言不允许重复创建。

---

### 3.6 更新编程语言信息

- **PUT** `/language`

**Request Body** (`LanguageSaveDTO`)：`id` 必填，其他字段选填。

---

### 3.7 启用编程语言

- **PUT** `/language/{id}/enable`

**Path 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 语言 ID |

**响应**：`data: true`

---

### 3.8 禁用编程语言

- **PUT** `/language/{id}/disable`

**Path 参数**：同 3.7。

> 禁用后，判题服务通过 Feign 调用 `/language/{id}` 时将收到 404 响应，提交代码时需在上层做拦截处理。

---

### 3.9 删除编程语言

- **DELETE** `/language/{id}`

> ⚠️ **物理删除**，请谨慎操作。建议先执行禁用（3.8），确认无影响后再删除。

---

## 5. 数据对象说明

### TestCaseVO

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 测试用例 ID |
| `problemId` | long | 关联题目 ID |
| `input` | string | 输入数据 |
| `output` | string | 预期输出 |
| `isSample` | int | 是否样例：0-否，1-是 |
| `score` | int | 分值 |
| `sortOrder` | int | 排序 |

### LanguageVO

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 语言 ID |
| `name` | string | 语言名称 |
| `version` | string | 版本号 |
| `compileCommand` | string | 编译命令模板 |
| `executeCommand` | string | 执行命令模板 |
| `sourceFileExt` | string | 源文件扩展名 |
| `executableExt` | string | 编译产物扩展名 |
| `isCompiled` | int | 是否需要编译 |
| `timeLimitMultiplier` | decimal | 时间限制乘数 |
| `memoryLimitMultiplier` | decimal | 内存限制乘数 |
| `status` | int | 状态：0-禁用，1-启用 |

---

## 错误码汇总

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

> 在线 Swagger UI 地址：`http://{problem-service-host}:9020/swagger-ui.html`
