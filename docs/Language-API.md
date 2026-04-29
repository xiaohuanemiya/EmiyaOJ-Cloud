# Programming Language API

本文档记录编程语言配置接口。语言配置由 Problem 服务维护，Judge 服务在判题时通过 `languageId` 查询该配置，并据此生成 go-judge 的编译与运行命令。

统一响应体：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## GET `/language/list`

查询启用的编程语言列表，供前端提交代码时选择语言。

Response `data`: `List<LanguageVO>`

```json
[
  {
    "id": 1,
    "name": "C++",
    "version": "C++20",
    "languageVersion": "c++20",
    "compileFileName": "main",
    "sourceFileExt": "cpp",
    "executableFileName": "main",
    "compiledFileNames": "main",
    "compileCommand": "/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp",
    "runCommand": "./{ExecutableFileName}",
    "envVars": "PATH=/usr/bin:/bin",
    "isCompiled": 1,
    "timeLimitMultiplier": 1.00,
    "memoryLimitMultiplier": 1.00,
    "compileTimeLimit": 10000,
    "compileMemoryLimit": 512,
    "compileProcLimit": 50,
    "runProcLimit": 1,
    "status": 1
  }
]
```

## GET `/language/{id}`

根据 ID 查询启用的编程语言详情。该接口供 Judge 服务 Feign 调用，也可供前端查看语言配置。

Path:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | Long | yes | 语言 ID |

Response `data`: `LanguageVO`

当语言不存在或已禁用时返回：

```json
{
  "code": 404,
  "message": "语言不存在或已禁用",
  "data": null
}
```

## GET `/language/admin/list`

管理端查询全部编程语言，包含禁用语言。

Response `data`: `List<LanguageVO>`

## GET `/language/admin/{id}`

管理端根据 ID 查询编程语言详情，不过滤启用状态。

Path:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | Long | yes | 语言 ID |

Response `data`: `LanguageVO`

## POST `/language`

新增编程语言配置。

Request body: `LanguageSaveDTO`

C++20 示例：

```json
{
  "name": "C++",
  "version": "C++20",
  "languageVersion": "c++20",
  "compileFileName": "main",
  "sourceFileExt": "cpp",
  "executableFileName": "main",
  "compiledFileNames": "main",
  "compileCommand": "/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp",
  "runCommand": "./{ExecutableFileName}",
  "envVars": "PATH=/usr/bin:/bin",
  "isCompiled": 1,
  "timeLimitMultiplier": 1.00,
  "memoryLimitMultiplier": 1.00,
  "compileTimeLimit": 10000,
  "compileMemoryLimit": 512,
  "compileProcLimit": 50,
  "runProcLimit": 1,
  "status": 1
}
```

Python3 示例：

```json
{
  "name": "Python3",
  "version": "Python 3.12",
  "languageVersion": "3.12",
  "compileFileName": "main",
  "sourceFileExt": "py",
  "executableFileName": "main.py",
  "compiledFileNames": null,
  "compileCommand": null,
  "runCommand": "/usr/bin/python3 {SourceFileName}",
  "envVars": "PATH=/usr/bin:/bin",
  "isCompiled": 0,
  "timeLimitMultiplier": 3.00,
  "memoryLimitMultiplier": 2.00,
  "compileTimeLimit": 10000,
  "compileMemoryLimit": 256,
  "compileProcLimit": 10,
  "runProcLimit": 1,
  "status": 1
}
```

Response `data`: `LanguageVO`

## PUT `/language`

更新编程语言配置。请求体与新增接口相同，但 `id` 必填。

Request body:

```json
{
  "id": 1,
  "name": "C++",
  "version": "C++20",
  "languageVersion": "c++20",
  "compileFileName": "main",
  "sourceFileExt": "cpp",
  "executableFileName": "main",
  "compiledFileNames": "main",
  "compileCommand": "/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp",
  "runCommand": "./{ExecutableFileName}",
  "envVars": "PATH=/usr/bin:/bin",
  "isCompiled": 1,
  "timeLimitMultiplier": 1.00,
  "memoryLimitMultiplier": 1.00,
  "compileTimeLimit": 10000,
  "compileMemoryLimit": 512,
  "compileProcLimit": 50,
  "runProcLimit": 1,
  "status": 1
}
```

Response `data`: `Boolean`

```json
true
```

## PUT `/language/{id}/enable`

启用编程语言。

Path:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | Long | yes | 语言 ID |

Response `data`: `Boolean`

## PUT `/language/{id}/disable`

禁用编程语言。

Path:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | Long | yes | 语言 ID |

Response `data`: `Boolean`

## DELETE `/language/{id}`

物理删除编程语言。

Path:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | Long | yes | 语言 ID |

Response `data`: `Boolean`

## DTO Fields

`LanguageVO` 与 `LanguageSaveDTO` 字段一致；`LanguageSaveDTO.id` 仅更新时必填。

| Field | Type | Required On Create | Description |
| --- | --- | --- | --- |
| `id` | Long | no | 语言 ID，更新时必填 |
| `name` | String | yes | 语言名称，如 `C++`、`Java`、`Python3` |
| `version` | String | yes | 展示版本，如 `C++20`、`Java 21` |
| `languageVersion` | String | yes | 命令模板中的语言版本值，如 `c++20` |
| `compileFileName` | String | no | 源文件基础名，不含扩展名，默认 `main` |
| `sourceFileExt` | String | yes | 源文件扩展名，不含点 |
| `executableFileName` | String | no | 运行目标名，默认 `main` |
| `compiledFileNames` | String | no | 编译产物文件名，多个用英文逗号分隔；为空时使用 `executableFileName` |
| `compileCommand` | String | compiled only | 编译命令模板，解释型语言可为空 |
| `runCommand` | String | yes | 运行命令模板 |
| `envVars` | String | no | GoJudge 环境变量，逗号或换行分隔，默认 `PATH=/usr/bin:/bin` |
| `isCompiled` | Integer | no | 是否需要编译：`0` 否，`1` 是，默认 `1` |
| `timeLimitMultiplier` | BigDecimal | no | 运行 CPU 时间倍数，默认 `1.00` |
| `memoryLimitMultiplier` | BigDecimal | no | 运行内存倍数，默认 `1.00` |
| `compileTimeLimit` | Integer | no | 编译 CPU 时间限制，毫秒，默认 `10000` |
| `compileMemoryLimit` | Integer | no | 编译内存限制，MB，默认 `512` |
| `compileProcLimit` | Integer | no | 编译进程数限制，默认 `50` |
| `runProcLimit` | Integer | no | 运行进程数限制，默认 `1` |
| `status` | Integer | no | 状态：`0` 禁用，`1` 启用，默认 `1` |

## Command Template Rules

命令模板支持以下占位符：

| Placeholder | Description |
| --- | --- |
| `{LanguageVersion}` | `languageVersion` |
| `{CompileFileName}` | `compileFileName` |
| `{SourceFileName}` | `compileFileName.sourceFileExt` |
| `{ExecutableFileName}` | `executableFileName` |

校验规则：

- `runCommand` 必填。
- `isCompiled = 1` 时，`compileCommand` 必填，且必须引用 `{CompileFileName}` 或 `{SourceFileName}`。
- `runCommand` 必须引用 `{ExecutableFileName}`、`{CompileFileName}` 或 `{SourceFileName}`。
- 命令模板不能包含换行、`;`、`|`、`&&`、`||`、反引号或 `$(`。
- 文件名字段不能包含路径，只能包含字母、数字、下划线、点和短横线。
- `sourceFileExt` 只能包含字母和数字。
- 资源倍数、时间限制、内存限制和进程数限制都必须大于 0。

## Judge Submit Relationship

`POST /judge/submit` 的请求体仍然只需要：

```json
{
  "problemId": 1,
  "languageId": 1,
  "code": "..."
}
```

其中 `languageId` 必须来自启用的 `/language/list`。Judge 服务会根据该 ID 获取语言配置，并使用 `compileCommand`、`runCommand`、文件名和资源限制生成 go-judge 调用参数。
