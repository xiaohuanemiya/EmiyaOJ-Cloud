# EmiyaOJ-Cloud — UML 2.0 完整建模文档

> **项目**: EmiyaOJ-Cloud 在线判题系统  
> **架构**: Spring Cloud 微服务 (Gateway + Auth + Problem + Judge + Blog + Chat + Moderation)  
> **建模标准**: UML 2.0 (Unified Modeling Language)  
> **图格式**: PlantUML  
> **建模日期**: 2026-05-20  

---

## 目录

| 章节 | 内容 | 图数 |
|------|------|------|
| [一、系统用例图](#一系统用例图-use-case-diagram) | 全系统参与者与用例关系 | 1 |
| [二、重点用例①：代码提交与自动判题](#二重点用例代码提交与自动判题) | 活动图 / 类图 / 时序图 / 通信图 / 构件图 / 部署图 | 6 |
| [三、重点用例②：用户认证与授权访问](#三重点用例用户认证与授权访问) | 活动图 / 类图 / 时序图 / 通信图 / 构件图 / 部署图 | 6 |
| [附录](#附录) | 与现有 Mermaid 文档交叉引用 | — |

> **总计 13 张 UML 2.0 图**  
> **PlantUML 渲染**: 复制代码块到 [PlantUML Online](https://www.plantuml.com/plantuml/uml/) 或使用 VS Code PlantUML 插件预览

---

## 一、系统用例图 (Use Case Diagram)

### 1.1 全系统用例图

涵盖 EmiyaOJ-Cloud 全部参与者与 8 大功能模块的用例划分。

```plantuml
@startuml EmiyaOJ-Cloud-UseCase
skinparam packageStyle rectangle
skinparam actorStyle awesome
left to right direction

actor "访客" as Visitor <<人员>>
actor "注册用户" as User <<人员>>
actor "管理员" as Admin <<人员>>
actor "审核人员" as Moderator <<人员>>
actor "Go-Judge\n判题沙箱" as GoJudge <<外部系统>>
actor "阿里云\n内容审核" as AliyunMod <<外部系统>>

rectangle "EmiyaOJ-Cloud 在线判题系统" as System {
  
  rectangle "认证模块" {
    usecase "用户注册" as UC_Register
    usecase "用户登录" as UC_Login
    usecase "用户登出" as UC_Logout
    usecase "Token 验证" as UC_Token
  }

  rectangle "题目模块" {
    usecase "浏览题目列表" as UC_BrowseProblem
    usecase "查看题目详情" as UC_ViewProblem
    usecase "创建/编辑题目" as UC_ManageProblem
    usecase "管理测试用例" as UC_ManageTestCase
    usecase "管理编程语言" as UC_ManageLanguage
    usecase "管理题目标签" as UC_ManageTag
  }

  rectangle "判题模块" {
    usecase "提交代码" as UC_SubmitCode
    usecase "查看提交记录" as UC_ViewSubmission
    usecase "查看判题结果" as UC_ViewResult
    usecase "执行代码编译运行" as UC_ExecuteJudge
  }

  rectangle "博客模块" {
    usecase "浏览博客" as UC_BrowseBlog
    usecase "发布/编辑博客" as UC_PublishBlog
    usecase "发表评论" as UC_Comment
    usecase "收藏/点赞博客" as UC_StarBlog
    usecase "管理博客标签" as UC_ManageBlogTag
  }

  rectangle "竞赛模块" {
    usecase "浏览竞赛" as UC_BrowseContest
    usecase "报名竞赛" as UC_RegisterContest
    usecase "创建/管理竞赛" as UC_ManageContest
    usecase "查看排行榜" as UC_ViewRanking
  }

  rectangle "AI 助手模块" {
    usecase "发送 AI 消息" as UC_AIChat
    usecase "获取代码建议" as UC_AISuggest
  }

  rectangle "审核模块" {
    usecase "自动文本审核" as UC_AutoMod
    usecase "人工审核复核" as UC_ManualMod
  }

  rectangle "管理模块" {
    usecase "用户管理" as UC_ManageUser
    usecase "角色管理" as UC_ManageRole
    usecase "权限管理" as UC_ManagePerm
    usecase "提交记录管理" as UC_ManageSubmission
  }
}

' === 访客 ===
Visitor --> UC_Register
Visitor --> UC_Login
Visitor --> UC_BrowseProblem
Visitor --> UC_ViewProblem
Visitor --> UC_BrowseBlog
Visitor --> UC_BrowseContest

' === 注册用户（继承访客） ===
User --|> Visitor
User --> UC_Logout
User --> UC_SubmitCode
User --> UC_ViewSubmission
User --> UC_ViewResult
User --> UC_PublishBlog
User --> UC_Comment
User --> UC_StarBlog
User --> UC_RegisterContest
User --> UC_ViewRanking
User --> UC_AIChat
User --> UC_AISuggest

' === 管理员（继承注册用户） ===
Admin --|> User
Admin --> UC_ManageProblem
Admin --> UC_ManageTestCase
Admin --> UC_ManageLanguage
Admin --> UC_ManageTag
Admin --> UC_ManageContest
Admin --> UC_ManageBlogTag
Admin --> UC_ManageUser
Admin --> UC_ManageRole
Admin --> UC_ManagePerm
Admin --> UC_ManageSubmission
Admin --> UC_ManualMod

' === 审核人员 ===
Moderator --> UC_ManualMod
Moderator --> UC_AutoMod

' === 外部系统参与 ===
GoJudge --> UC_ExecuteJudge
AliyunMod --> UC_AutoMod

' === 用例间关系 ===
UC_SubmitCode ..> UC_ExecuteJudge : <<include>>
UC_ManageSubmission ..> UC_ViewResult : <<extend>>
UC_PublishBlog ..> UC_AutoMod : <<include>>
UC_Comment ..> UC_AutoMod : <<include>>
UC_SubmitCode ..> UC_Token : <<include>>
UC_RegisterContest ..> UC_Token : <<include>>

@enduml
```

### 1.2 用例图说明

| 要素 | 说明 |
|------|------|
| 4 类人员参与者 | 访客、注册用户、管理员、审核人员 |
| 2 类外部系统 | Go-Judge 判题沙箱、阿里云内容审核服务 |
| 9 个功能模块 | 认证、题目、判题、博客、竞赛、AI助手、审核、管理 |
| 25 个用例 | 覆盖用户端和管理端全部功能 |
| 关键关系 | `<<include>>` = 必须包含；`<<extend>>` = 可选扩展；`--|>` = 角色继承 |

---

## 二、重点用例 ①：代码提交与自动判题

> **用例编号**: UC-JUDGE-001  
> **参与角色**: 注册用户（主参与者）、Go-Judge沙箱（辅助参与者）  
> **前置条件**: 用户已登录，题目存在且公开，编程语言已启用  
> **后置条件**: 系统创建提交记录，异步完成判题并更新状态  

---

### 2.1 活动图 (Activity Diagram)

描述从用户提交代码到判题结果生成的完整业务流程。

```plantuml
@startuml Judge-Activity
|用户端|
start
:选择题目和编程语言;
:编写/粘贴源代码;
:点击"提交代码";
|Gateway|
:接收请求，提取 Token;
if (Token 有效?) then (否)
  :返回 401 未授权;
  stop
else (是)
  :注入 X-User-Id 请求头;
endif
|Judge Service|
:接收提交请求;
:校验题目是否存在;
if (题目存在且公开?) then (否)
  :返回"题目不可用";
  stop
else (是)
endif
:校验语言是否启用;
if (语言已启用?) then (否)
  :返回"语言不可用";
  stop
else (是)
endif
:创建 Submission 记录\n(status = 0 PENDING);
:返回 submissionId 给用户;
|#LightBlue|异步判题 (JudgeExecutor)|
fork
  :更新状态为 JUDGING(1);
  |Problem Service|
  :Feign 调用获取题目详情;
  :Feign 调用获取语言配置;
  :Feign 调用获取测试用例列表;
  |Judge Service|
  if (语言需要编译?) then (是)
    :调用 GoJudgeService.compile();
    |Go-Judge 沙箱|
    :在沙箱中编译源代码;
    if (编译成功?) then (否)
      :返回 CE (编译错误);
      |Judge Service|
      :保存编译错误信息;
      :更新状态为 CE(3);
      :更新结束时间;
      stop
    else (是)
    endif
  else (否)
  endif
  :逐测试用例运行;
  while (还有未运行用例?) is (是)
    :调用 GoJudgeService.run()\n传入用例输入和资源限制;
    |Go-Judge 沙箱|
    :在沙箱中运行代码;
    :返回运行结果 (状态/输出/耗时/内存);
    |Judge Service|
    :比对输出和标准答案;
    :保存 SubmissionCaseResult;
    if (超时?) then (是)
      :标记 TLE;
    elseif (超内存?) then (是)
      :标记 MLE;
    elseif (运行异常?) then (是)
      :标记 RE;
    elseif (输出不匹配?) then (是)
      :标记 WA;
    else (是)
      :标记 AC (通过);
    endif
  endwhile (否)
  :汇总最终判题状态;
  :计算总分和通过率;
  :更新 Submission 记录\n(status / score / time / memory);
  :更新结束时间;
  :更新 Problem 提交数/通过数;
end fork
|用户端|
:轮询或刷新查看判题结果;
:展示状态及详细结果;
stop
@enduml
```

---

### 2.2 类图 (Class Diagram)

展示判题模块的核心实体类、服务类、以及跨服务 Feign 调用关系。

```plantuml
@startuml Judge-Class
skinparam classAttributeIconSize 0

' === 实体类 (Domain) ===
class Submission {
  - Long id
  - Long problemId
  - Long userId
  - Long languageId
  - Long contestId
  - String code
  - Integer status
  - Integer score
  - Long maxTimeUsed
  - Long maxMemoryUsed
  - String errorMessage
  - String compileMessage
  - Integer passedCaseCount
  - Integer totalCaseCount
  - LocalDateTime createTime
  - LocalDateTime finishTime
  + getId(): Long
  + getStatus(): Integer
}

class SubmissionCaseResult {
  - Long id
  - Long submissionId
  - Long testCaseId
  - Integer caseOrder
  - Integer status
  - Long timeUsed
  - Long memoryUsed
  - String actualOutput
  - String errorMessage
}

class SubmissionJudgeResult {
  - Long id
  - Long submissionId
  - Integer status
  - Integer score
  - String compileOutput
  - String judgeMessage
}

class GoJudgeRequest {
  - List<Cmd> cmd
  - Map pipeMapping
}

class GoJudgeResult {
  - String status
  - Integer exitStatus
  - Long time
  - Long memory
  - Long runTime
  - Map files
  - Map fileIds
  - String error
}

class Cmd {
  - List<String> args
  - Map env
  - Map files
  - Long cpuLimit
  - Long realCpuLimit
  - Long memoryLimit
  - Long stackLimit
  - Integer procLimit
  - Boolean strictMemoryLimit
  - String input
  - String output
}

enum JudgeStatus {
  PENDING = 0
  JUDGING = 1
  ACCEPTED = 2
  COMPILE_ERROR = 3
  SYSTEM_ERROR = 4
  WRONG_ANSWER = 5
  TIME_LIMIT_EXCEEDED = 6
  MEMORY_LIMIT_EXCEEDED = 7
  RUNTIME_ERROR = 8
  OUTPUT_LIMIT_EXCEEDED = 9
  PARTIAL_ACCEPTED = 10
}

' === DTO ===
class SubmitCodeDTO {
  - Long problemId
  - Long languageId
  - String code
  - Long contestId
}

class SubmissionVO {
  - Long id
  - Long problemId
  - Long userId
  - Long languageId
  - Integer status
  - Integer passedCaseCount
  - Integer totalCaseCount
  - Integer score
  - Long maxTimeUsed
  - Long maxMemoryUsed
  - String errorMessage
  - String compileMessage
  - LocalDateTime createTime
  - LocalDateTime finishTime
}

' === Controller ===
class JudgeController {
  + submitCode(SubmitCodeDTO, userId): ResponseResult<SubmissionVO>
}

' === Service ===
class SubmissionService {
  + submitCode(SubmitCodeDTO, userId): SubmissionVO
  + getSubmissionById(id): SubmissionVO
  + getSubmissionPage(dto, problemId, userId): PageVO<SubmissionVO>
  + getMySubmissions(dto, userId, problemId): PageVO<SubmissionVO>
}

class JudgeExecutor {
  + executeJudgeAsync(submissionId, problemId, languageId, code): void
  - compileCode(code, language): GoJudgeResult
  - runTestCase(code, lang, testCase, tl, ml): GoJudgeResult
  - determineFinalStatus(caseResults): JudgeStatus
}

class GoJudgeService {
  + compile(code, languageConfig): GoJudgeResult
  + run(languageConfig, fileIds, code, input, timeLimit, memoryLimit): GoJudgeResult
  + healthCheck(): Boolean
}

' === Feign Client ===
interface ProblemFeignClient {
  + getProblemById(problemId): ResponseResult<ProblemVO>
  + getTestCasesByProblemId(problemId): ResponseResult<List<TestCaseVO>>
  + getLanguageById(languageId): ResponseResult<LanguageVO>
  + getSubmissionCaseResult(submissionId): ResponseResult<List<TestCaseResultVO>>
}

' === 引用外部实体 (跨服务) ===
class ProblemVO <<external>> {
  - Long id
  - String title
  - Integer timeLimit
  - Integer memoryLimit
  - Integer stackLimit
}

class LanguageVO <<external>> {
  - Long id
  - String name
  - String compileCommand
  - String runCommand
  - Integer isCompiled
  - Double timeLimitMultiplier
  - Double memoryLimitMultiplier
}

class TestCaseVO <<external>> {
  - Long id
  - String input
  - String output
  - Integer isSample
  - Integer score
}

' === Mapper ===
interface SubmissionMapper {
  + insert(submission): int
  + selectById(id): Submission
  + selectPage(page, queryWrapper): Page<Submission>
  + updateById(submission): int
}

' === 关系 ===
JudgeController --> SubmissionService : 调用
SubmissionService --> SubmissionMapper : 数据访问
SubmissionService --> JudgeExecutor : 异步触发
JudgeExecutor --> ProblemFeignClient : Feign调用
JudgeExecutor --> GoJudgeService : 沙箱调用
JudgeExecutor --> SubmissionMapper : 更新状态

Submission "1" -- "*" SubmissionCaseResult : 包含
Submission "1" -- "0..1" SubmissionJudgeResult : 汇总
GoJudgeRequest "1" *-- "*" Cmd : 组合
GoJudgeService ..> GoJudgeRequest : 构造
GoJudgeService ..> GoJudgeResult : 返回
Submission ..> JudgeStatus : 使用
JudgeController ..> SubmitCodeDTO : 接收
JudgeController ..> SubmissionVO : 返回

ProblemFeignClient ..> ProblemVO : 获取
ProblemFeignClient ..> LanguageVO : 获取
ProblemFeignClient ..> TestCaseVO : 获取

note right of JudgeExecutor
  @Async 异步执行
  使用 @EnableAsync
end note

note bottom of GoJudgeService
  HTTP 调用 Go-Judge 沙箱
  REST API: POST /api/judge
end note

@enduml
```

---

### 2.3 时序图 (Sequence Diagram)

展示代码提交与异步判题的完整交互消息链。

```plantuml
@startuml Judge-Sequence
skinparam ParticipantPadding 20
skinparam BoxPadding 10

actor "用户" as User
participant "用户端\n(Frontend)" as FE
participant "Gateway\n:8080" as GW
participant "JudgeController\n:9030" as JC
participant "SubmissionService" as SS
participant "JudgeExecutor\n(@Async)" as JE
participant "ProblemFeignClient" as PFC
participant "Problem\nService\n:9020" as PS
participant "GoJudgeService" as GJS
participant "Go-Judge\n沙箱 :5050" as GJ
database "MySQL\n(emiya_oj_judge)" as DB

== 阶段1: 代码提交 ==
User -> FE: 选择题目、语言\n粘贴代码并提交
FE -> GW: POST /judge/submit\n{problemId, languageId, code}
GW -> GW: AuthGlobalFilter\n解析JWT & 验证Redis白名单
GW -> JC: 转发请求\n(Header: X-User-Id)
JC -> SS: submitCode(dto, userId)
SS -> DB: INSERT submission\n(status = 0 PENDING)
SS --> JC: SubmissionVO (submissionId)
JC --> GW: ResponseResult<SubmissionVO>
GW --> FE: {code:200, data: {id, status:0}}
FE --> User: 展示"判题中..."

== 阶段2: 异步判题 ==
SS -> JE: @Async executeJudgeAsync\n(submissionId, problemId, languageId, code)

activate JE
JE -> DB: UPDATE status = 1 (JUDGING)

JE -> PFC: getProblemById(problemId)
PFC -> PS: GET /problem/{id}
PS --> PFC: ProblemVO (timeLimit, memoryLimit...)
PFC --> JE: ProblemVO

JE -> PFC: getLanguageById(languageId)
PFC -> PS: GET /language/{id}
PS --> PFC: LanguageVO (编译命令, 运行命令...)
PFC --> JE: LanguageVO

JE -> PFC: getTestCasesByProblemId(problemId)
PFC -> PS: GET /test-case/problem/{problemId}
PS --> PFC: List<TestCaseVO>
PFC --> JE: List<TestCaseVO>

alt 编译型语言
  JE -> GJS: compile(code, languageConfig)
  GJS -> GJ: HTTP POST /api/judge\n(compile cmd)
  GJ --> GJS: GoJudgeResult (编译结果)
  GJS --> JE: GoJudgeResult
  alt 编译失败
    JE -> DB: UPDATE status = 3 (CE)\ncompileMessage = 错误信息
    deactivate JE
  else 编译成功
    note right of JE: 获取编译后的 fileIds
  end
end

loop 逐个测试用例
  JE -> GJS: run(language, fileIds, code, input, timeLimit, memoryLimit)
  GJS -> GJ: HTTP POST /api/judge\n(run cmd + input)
  GJ --> GJS: GoJudgeResult\n(status, time, memory, output)
  GJS --> JE: GoJudgeResult

  JE -> JE: 比对 output 与 标准答案
  JE -> DB: INSERT SubmissionCaseResult\n(status, timeUsed, memoryUsed)
end

JE -> JE: 汇总最终状态\n(AC/WA/TLE/PA...)
JE -> DB: UPDATE submission\nSET status, score, maxTimeUsed,\nmaxMemoryUsed, passedCaseCount,\nfinishTime
JE -> DB: INSERT SubmissionJudgeResult\n(汇总结果)
deactivate JE

== 阶段3: 用户查看结果 ==
User -> FE: 刷新提交详情页
FE -> GW: GET /submission/{id}
GW -> JC: 转发
JC -> SS: getSubmissionById(id)
SS -> DB: SELECT submission + caseResults
SS --> JC: SubmissionVO (完整结果)
JC --> FE: 判题详情
FE --> User: 展示状态、得分、\n通过用例、耗时、内存

@enduml
```

---

### 2.4 通信图 (Communication Diagram)

展示判题链路中各对象间的链接与消息传递（与 2.3 时序图对应）。

```plantuml
@startuml Judge-Communication
skinparam rectangleBorderColor #1565C0
skinparam rectangleBackgroundColor #E3F2FD
skinparam defaultFontSize 12

rectangle "用户端\n(Frontend)" as FE
rectangle "Gateway\n:8080" as GW
rectangle "JudgeController\nJudgeService" as JC [
**JudgeController**
--
+ submitCode()
--
**SubmissionService**
--
+ submitCode()
+ getSubmissionById()
]
rectangle "JudgeExecutor\n(@Async)" as JE [
**JudgeExecutor**
--
+ executeJudgeAsync()
- compileCode()
- runTestCase()
]
rectangle "GoJudgeService" as GJS [
**GoJudgeService**
--
+ compile()
+ run()
]
rectangle "ProblemService\n:9020" as PS [
**ProblemService**
--
题目/语言/测试用例
数据源
]
rectangle "Go-Judge 沙箱\n:5050" as GJ
database "MySQL\n(emiya_oj_judge)" as DB

' === 交互链接 ===
FE -[#blue]> GW : 1: POST /judge/submit
GW -[#blue]> JC : 2: 路由转发\n(X-User-Id)
JC -[#blue]> DB : 3: INSERT submission\n(PENDING)
JC -[#blue]> JE : 4: @Async 触发判题

JE -[#green]> PS : 5: Feign 调用\n获取题目/语言/测试用例
JE -[#green]> GJS : 6: 调用编译服务

GJS -[#red]> GJ : 7: HTTP POST\n编译/运行代码
GJ -[#red]> GJS : 8: 返回运行结果

JE -[#green]> GJS : 9: 逐用例运行
GJS -[#red]> GJ : 10: HTTP POST\n每个测试用例

JE -[#blue]> DB : 11: INSERT SubmissionCaseResult
JE -[#blue]> DB : 12: UPDATE/INSERT\n汇总结果

FE -[#blue]> GW : 13: GET /submission/{id}
GW -[#blue]> JC : 14: 路由转发
JC -[#blue]> DB : 15: SELECT 提交详情
JC -[#blue]> FE : 16: 返回完整结果

note bottom of JE
  @Async异步执行
  Spring异步线程池
end note

@enduml
```

---

### 2.5 构件图 (Component Diagram)

展示判题链路涉及的软件构件及其接口依赖。

```plantuml
@startuml Judge-Component
skinparam componentStyle rectangle

package "EmiyaOJ-Gateway" {
  [Gateway\nApplication] as GW
  [AuthGlobalFilter] as GF
  GW --> GF
}

package "EmiyaOJ-Judge" {
  [judge-api] as JA <<api>>
  [judge-service] as JS <<service>>
  [JudgeExecutor\n(@Async)] as JE
  [GoJudgeService] as GJS
  JA --> JS : 实现
  JS --> JE : 异步调用
  JE --> GJS : HTTP调用
}

package "EmiyaOJ-Problem" {
  [problem-api] as PA <<api>>
  [problem-service] as PS <<service>>
  PA --> PS : 实现
}

package "Go-Judge 沙箱" {
  [Go-Judge\nREST API] as GJ
}

package "EmiyaOJ-Common" {
  [ResponseResult]
  [PageDTO/PageVO]
  [JwtUtil]
  [RedisUtil]
}

database "MySQL\nemiya_oj_judge" as JDB
database "MySQL\nemiya_oj_problem" as PDB
database "Redis" as Redis

' === 接口 ===
rectangle "REST API\nPOST /judge/submit" as SubmitAPI
rectangle "REST API\nGET /submission/**" as QueryAPI
rectangle "Feign\nProblemFeignClient" as FeignPC
rectangle "HTTP\nPOST /api/judge" as JudgeAPI
rectangle "JDBC" as JDBC1
rectangle "JDBC" as JDBC2

GW --> SubmitAPI : 暴露
GW --> QueryAPI : 暴露
SubmitAPI --> JA
QueryAPI --> JA

JS --> FeignPC : 依赖
FeignPC --> PA : 调用

GJS --> JudgeAPI : 调用
JudgeAPI --> GJ

JA --> JDB : JDBC
PS --> PDB : JDBC
GW --> Redis : Redis协议

note right of GJ
  独立沙箱服务
  编译与运行用户代码
  资源隔离执行
end note

@enduml
```

---

### 2.6 部署图 (Deployment Diagram)

展示判题链路的物理部署节点及通信协议。

```plantuml
@startuml Judge-Deployment
skinparam nodeBorderColor #333333
skinparam nodeBackgroundColor #FAFAFA
skinparam defaultFontSize 11

node "用户浏览器" as Browser {
  component "Vue/React\n前端应用" as WebApp
}

node "Docker 宿主机\n(10.0.0.10)" as DockerHost {
  
  node "Gateway 容器\nemiya-oj-gateway:1.0\nPort: 8080" as GW {
    [Spring Cloud Gateway]
  }

  node "Judge 容器\nemiya-oj-judge:1.0\nPort: 9030" as Judge {
    [Judge Service\nSpring Boot]
    [JudgeExecutor\n异步线程池]
  }

  node "Problem 容器\nemiya-oj-problem:1.0\nPort: 9020" as Problem {
    [Problem Service\nSpring Boot]
  }

  node "Go-Judge 容器\ngo-judge:latest\nPort: 5050" as GoJudge {
    [Go-Judge\n判题沙箱]
  }

  node "MySQL 容器\nmysql:8.0\nPort: 3306" as MySQL {
    database "emiya_oj_judge"
    database "emiya_oj_problem"
  }

  node "Redis 容器\nredis:7-alpine\nPort: 6379" as Redis {
    database "Token 白名单"
  }

  node "Nacos 容器\nnacos:v2.5.1\nPort: 8848" as Nacos {
    [服务注册中心]
    [配置中心]
  }
}

' === 通信协议 ===
Browser -[#blue]-> GW : <<HTTPS/HTTP>>
GW -[#green]-> Judge : <<HTTP\n路由转发>>
GW -[#green]-> Problem : <<HTTP\n路由转发>>
Judge -[#orange]-> Problem : <<Feign\nHTTP/负载均衡>>
Judge -[#red]-> GoJudge : <<HTTP\nREST API>>
Judge -[#purple]-> MySQL : <<JDBC\nTCP 3306>>
Problem -[#purple]-> MySQL : <<JDBC\nTCP 3306>>
GW -[#teal]-> Redis : <<Redis\nTCP 6379>>
Judge -.-> Nacos : <<服务注册/发现>>
Problem -.-> Nacos : <<服务注册/发现>>
GW -.-> Nacos : <<服务发现>>

note right of GoJudge
  安全隔离：
  - 独立容器运行
  - 限制 CPU/内存
  - 无网络访问
  - 文件系统隔离
end note

note bottom of DockerHost
  Docker Compose 编排
  -- 网络: emiya-net (bridge)
  -- 重启策略: unless-stopped
  -- 日志驱动: json-file
end note

@enduml
```

---

## 三、重点用例 ②：用户认证与授权访问

> **用例编号**: UC-AUTH-001  
> **参与角色**: 访客（注册/登录）、注册用户（登出/鉴权访问）、管理员（RBAC 管理）  
> **前置条件**: 系统已部署 Gateway / Auth / Redis / MySQL  
> **后置条件**: 用户获得 JWT Token，可访问受保护资源  

---

### 3.1 活动图 (Activity Diagram)

描述用户登录、Token 签发、请求鉴权与 RBAC 权限校验的完整流程。

```plantuml
@startuml Auth-Activity
|用户|
start
:访问系统;
|Gateway|
if (请求路径在白名单?) then (是)
  :直接放行\n(登录/注册/公开题目/公开博客);
  if (是登录请求?) then (是)
    |Auth Service|
    :接收用户名密码;
    :调用 UserDetailsService.loadUserByUsername();
    :校验密码 (PasswordEncoder);
    if (密码正确且账号启用?) then (否)
      :返回"用户名或密码错误";
      stop
    else (是)
      :查询用户角色和权限;
      :生成 JWT Token\n(JwtUtil.createJWT);
      :将 Token 写入 Redis 白名单\n(key: token_{userId});
      :返回 UserLoginVO\n(id, username, nickname, token);
      |用户|
      :前端存储 Token;
      stop
    endif
  else (其他白名单路径)
    :直接返回公开资源;
    stop
  endif
else (否)
endif

|Gateway|
if (请求头包含 Authorization: Bearer?) then (否)
  :返回 401 未授权;
  stop
else (是)
  :提取 Bearer Token;
  :JwtUtil.parseJWT() 解析 Token;
  if (JWT 解析成功且未过期?) then (否)
    :返回 401 Token 无效或过期;
    stop
  else (是)
    :提取 userId from Claims;
    :Redis.get("token_" + userId);
    if (Redis 中存在且值匹配?) then (否)
      :返回 401 Token 已失效(已登出);
      stop
    else (是)
      :注入请求头:\n- X-User-Id\n- X-User-Name\n- X-User-Roles;
    endif
  endif
endif

|下游微服务|
:接收请求 (含用户上下文);
if (接口需要特定权限?) then (是)
  :从 X-User-Roles 中检查权限;
  if (有权限?) then (否)
    :返回 403 无权限;
    stop
  else (是)
    :执行业务逻辑;
  endif
else (否)
  :执行业务逻辑;
endif
:返回 ResponseResult;
stop

|#LightPink|用户登出|
|用户|
:点击"退出登录";
|Gateway|
:POST /auth/logout;
|Auth Service|
:删除 Redis 中 Token 白名单记录;
:返回操作成功;
|用户|
:清除本地 Token;
stop

@enduml
```

---

### 3.2 类图 (Class Diagram)

展示认证授权模块的核心实体类、服务类、Spring Security 集成及网关过滤器。

```plantuml
@startuml Auth-Class
skinparam classAttributeIconSize 0

' === 实体类 ===
class User {
  - Long id
  - String username
  - String password
  - String nickname
  - String email
  - String phone
  - String avatar
  - Integer status
  - Integer deleted
  - LocalDateTime createTime
  - LocalDateTime updateTime
  - Long createBy
  - Long updateBy
  + getAuthorities(): Collection<GrantedAuthority>
}

class Role {
  - Long id
  - String roleCode
  - String roleName
  - String description
  - Integer status
  - Integer deleted
  - LocalDateTime createTime
  - LocalDateTime updateTime
}

class Permission {
  - Long id
  - Long parentId
  - String permissionCode
  - String permissionName
  - Integer permissionType
  - String path
  - String component
  - String icon
  - Integer sortOrder
  - Integer status
  - List<Permission> children
}

class UserRole {
  - Long id
  - Long userId
  - Long roleId
  - LocalDateTime createTime
  - Long createBy
}

class RolePermission {
  - Long roleId
  - Long permissionId
  - LocalDateTime createTime
}

class LoginUser {
  - User user
  - Collection<GrantedAuthority> authorities
  - List<String> permissions
  + getAuthorities(): Collection<GrantedAuthority>
  + getPassword(): String
  + getUsername(): String
  + isAccountNonLocked(): boolean
  + isEnabled(): boolean
}

enum PermissionTypeEnum {
  MENU = 1
  BUTTON = 2
  API = 3
}

' === DTO/VO ===
class UserLoginDTO {
  - String username
  - String password
}

class UserLoginVO {
  - Long id
  - String username
  - String nickname
  - String token
}

class UserAuthDTO {
  - Long userId
  - String username
  - List<String> permissions
}

class UserSaveDTO {
  - Long id
  - String username
  - String password
  - String nickname
  - String email
  - String phone
  - Integer status
}

class RoleSaveDTO {
  - Long id
  - String roleCode
  - String roleName
  - String description
  - Integer status
}

class PermissionSaveDTO {
  - Long id
  - Long parentId
  - String permissionCode
  - String permissionName
  - Integer permissionType
  - String path
  - Integer sortOrder
  - Integer status
}

' === Controller ===
class AuthController {
  + login(UserLoginDTO): ResponseResult<UserLoginVO>
  + logout(userId): ResponseResult<?>
  + parseToken(token): ResponseResult<UserAuthDTO>
}

class UserController {
  + page(PageDTO): ResponseResult<PageVO<UserVO>>
  + getById(id): ResponseResult<UserVO>
  + save(UserSaveDTO): ResponseResult<Void>
  + update(UserSaveDTO): ResponseResult<Void>
  + delete(id): ResponseResult<Void>
  + resetPassword(id): ResponseResult<Void>
  + updateStatus(id, status): ResponseResult<Void>
  + assignRoles(id, roleIds): ResponseResult<Void>
  + hasPermission(id, code): ResponseResult<Boolean>
  + hasRole(id, code): ResponseResult<Boolean>
}

class RoleController {
  + page(RoleQueryDTO): ResponseResult<PageVO<RoleVO>>
  + list(): ResponseResult<List<RoleVO>>
  + getById(id): ResponseResult<RoleVO>
  + save(RoleSaveDTO): ResponseResult<Void>
  + update(RoleSaveDTO): ResponseResult<Void>
  + delete(id): ResponseResult<Void>
  + updateStatus(id, status): ResponseResult<Void>
  + assignPermissions(id, permIds): ResponseResult<Void>
  + exists(code): ResponseResult<Boolean>
}

class PermissionController {
  + list(PermissionQueryDTO): ResponseResult<List<PermissionVO>>
  + tree(PermissionQueryDTO): ResponseResult<List<PermissionVO>>
  + getById(id): ResponseResult<PermissionVO>
  + save(PermissionSaveDTO): ResponseResult<Void>
  + update(PermissionSaveDTO): ResponseResult<Void>
  + delete(id): ResponseResult<Void>
  + updateStatus(id, status): ResponseResult<Void>
  + exists(code): ResponseResult<Boolean>
}

' === Service ===
class AuthService {
  + login(UserLoginDTO): UserLoginVO
  + logout(userId): void
  + parseToken(token): UserAuthDTO
}

class UserServiceImpl {
  + selectUserPage(PageDTO): PageVO<UserVO>
  + selectUserById(id): UserVO
  + saveUser(UserSaveDTO): void
  + updateUser(UserSaveDTO): void
  + deleteUser(id): void
  + resetPassword(id): void
  + updateUserStatus(id, status): void
  + assignRoles(id, roleIds): void
  + hasPermission(id, code): Boolean
  + hasRole(id, code): Boolean
}

class RoleServiceImpl {
  + selectRolePage(RoleQueryDTO): PageVO<RoleVO>
  + selectAllRoles(): List<RoleVO>
  + selectRoleById(id): RoleVO
  + saveRole(RoleSaveDTO): void
  + updateRole(RoleSaveDTO): void
  + deleteRole(id): void
  + updateRoleStatus(id, status): void
  + assignPermissions(id, permIds): void
  + existsRoleCode(code): Boolean
}

class PermissionServiceImpl {
  + selectPermissionList(dto): List<PermissionVO>
  + selectPermissionTree(dto): List<PermissionVO>
  + selectPermissionById(id): PermissionVO
  + savePermission(dto): void
  + updatePermission(dto): void
  + deletePermission(id): void
  + updatePermissionStatus(id, status): void
  + existsPermissionCode(code): Boolean
}

' === Spring Security ===
class UserDetailsServiceImpl {
  + loadUserByUsername(username): UserDetails
}

class SecurityConfig {
  + authenticationManager(): AuthenticationManager
  + passwordEncoder(): PasswordEncoder
  + securityFilterChain(http): SecurityFilterChain
}

' === Gateway Filter ===
class AuthGlobalFilter {
  - JwtUtil jwtUtil
  - RedisUtil redisUtil
  - GatewayWhitelistProperties whitelist
  + filter(exchange, chain): Mono<Void>
  - isWhitelistPath(path): Boolean
  - extractToken(request): String
  - validateToken(token): Claims
  - injectUserHeaders(exchange, claims): void
}

' === Common 工具 ===
class JwtUtil {
  - String secretKey
  - Long ttlMillis
  + createJWT(claims): String
  + parseJWT(token): Claims
  - getKey(): SecretKey
}

class RedisUtil {
  - StringRedisTemplate redisTemplate
  + set(key, value): void
  + set(key, value, ttl): void
  + get(key): String
  + delete(key): void
  + exists(key): Boolean
}

' === Mapper ===
interface UserMapper
interface RoleMapper
interface PermissionMapper
interface UserRoleMapper
interface RolePermissionMapper

' ====== 关系 ======

' 实体关系
User "1" -- "*" UserRole : 拥有
Role "1" -- "*" UserRole : 被分配
Role "1" -- "*" RolePermission : 包含
Permission "1" -- "*" RolePermission : 被授予
Permission "1" -- "*" Permission : 父子递归

' Controller → Service
AuthController --> AuthService : 调用
UserController --> UserServiceImpl : 调用
RoleController --> RoleServiceImpl : 调用
PermissionController --> PermissionServiceImpl : 调用

' Service → Mapper
UserServiceImpl --> UserMapper : 数据访问
RoleServiceImpl --> RoleMapper : 数据访问
PermissionServiceImpl --> PermissionMapper : 数据访问

' AuthService 依赖
AuthService --> UserDetailsServiceImpl : 认证
AuthService --> JwtUtil : 生成Token
AuthService --> RedisUtil : 白名单管理

' Security
UserDetailsServiceImpl ..|> UserDetailsService <<interface>>
UserDetailsServiceImpl --> UserMapper : 查询用户
SecurityConfig --> UserDetailsServiceImpl : 配置
LoginUser ..|> UserDetails <<interface>>
LoginUser --> User : 包装

' Gateway Filter
AuthGlobalFilter --> JwtUtil : 解析Token
AuthGlobalFilter --> RedisUtil : 验证白名单

' DTO 使用
AuthController ..> UserLoginDTO : 接收
AuthController ..> UserLoginVO : 返回
UserController ..> UserSaveDTO : 接收
RoleController ..> RoleSaveDTO : 接收
PermissionController ..> PermissionSaveDTO : 接收
Permission ..> PermissionTypeEnum : 类型

note right of AuthGlobalFilter
  implements GlobalFilter, Ordered
  所有请求经过此过滤器
  白名单路径直接放行
  非白名单路径需Token校验
end note

note bottom of SecurityConfig
  @Configuration
  @EnableWebSecurity
  Spring Security 6.x
end note

@enduml
```

---

### 3.3 时序图 (Sequence Diagram)

展示 (a) 用户登录流程 和 (b) 请求鉴权流程。

#### 3.3.1 用户登录流程

```plantuml
@startuml Auth-Login-Sequence
skinparam ParticipantPadding 20

actor "用户" as User
participant "用户端\n(Frontend)" as FE
participant "Gateway\n:8080" as GW
participant "AuthController\n:9010" as AC
participant "AuthService" as AS
participant "UserDetailsServiceImpl" as UDS
participant "JwtUtil" as JWT
participant "RedisUtil" as Redis
database "MySQL\n(emiya_oj_auth)" as DB

User -> FE: 输入用户名密码\n点击登录
FE -> GW: POST /auth/login\n{username, password}
GW -> GW: 路径 /auth/login\n在白名单中 → 放行
GW -> AC: 转发登录请求

AC -> AS: login(UserLoginDTO)
AS -> UDS: loadUserByUsername(username)
UDS -> DB: SELECT * FROM user\nWHERE username = ?
DB --> UDS: User 实体
UDS --> AS: UserDetails (LoginUser)

AS -> AS: PasswordEncoder.matches()\n校验密码
AS -> AS: 检查账号状态 (status=1)

AS -> DB: 查询用户角色\n(user_role + role)
DB --> AS: List<Role>
AS -> DB: 查询角色权限\n(role_permission + permission)
DB --> AS: List<Permission>

AS -> JWT: createJWT(secretKey, ttl, claims)
JWT --> AS: JWT Token String

AS -> Redis: set("token_" + userId, token, ttl)
Redis --> AS: OK

AS --> AC: UserLoginVO (id, username, nickname, token)
AC --> GW: ResponseResult<UserLoginVO>
GW --> FE: {code:200, data: {token, userInfo}}
FE -> FE: 存储 Token 到 localStorage
FE --> User: 跳转到首页/个人中心

@enduml
```

#### 3.3.2 请求鉴权流程

```plantuml
@startuml Auth-Request-Sequence
skinparam ParticipantPadding 20

actor "用户" as User
participant "用户端\n(Frontend)" as FE
participant "Gateway\n:8080" as GW
participant "AuthGlobalFilter" as AGF
participant "JwtUtil" as JWT
participant "RedisUtil" as Redis
participant "下游服务\n(Problem/Judge/Blog...)" as DS

User -> FE: 访问受保护资源\n(如: 提交代码)
FE -> GW: GET/POST /**\n(Header: Authorization: Bearer xxx)

GW -> AGF: filter(exchange, chain)

AGF -> AGF: isWhitelistPath(path)
note right: 非白名单路径\n继续鉴权

AGF -> AGF: extractToken(request)
note right: 从 Authorization 头\n提取 Bearer Token

AGF -> JWT: parseJWT(secretKey, token)
alt JWT 解析失败或过期
  JWT --> AGF: Exception
  AGF -> FE: 返回 401 Unauthorized
else JWT 解析成功
  JWT --> AGF: Claims {userId, username, ...}
end

AGF -> AGF: 提取 userId from Claims
AGF -> Redis: get("token_" + userId)
alt Redis 中不存在或值不匹配
  Redis --> AGF: null
  AGF -> FE: 返回 401 Token已失效
else Redis 中存在且匹配
  Redis --> AGF: token
end

AGF -> AGF: injectUserHeaders(exchange, claims)
note right: 注入请求头:\nX-User-Id: userId\nX-User-Name: username\nX-User-Roles: permissions

AGF -> DS: chain.filter()\n转发到下游服务

DS -> DS: 从请求头读取用户上下文
DS -> DS: 执行业务逻辑\n(如需权限: 检查 X-User-Roles)
DS --> GW: ResponseResult
GW --> FE: 返回业务数据
FE --> User: 展示结果

@enduml
```

---

### 3.4 通信图 (Communication Diagram)

展示认证鉴权链路中各对象间的链接与消息传递。

```plantuml
@startuml Auth-Communication
skinparam rectangleBorderColor #6A1B9A
skinparam rectangleBackgroundColor #F3E5F5
skinparam defaultFontSize 12

rectangle "用户端\n(Frontend)" as FE
rectangle "Gateway\n:8080\nAuthGlobalFilter" as GW [
**AuthGlobalFilter**
--
+ filter()
- isWhitelistPath()
- extractToken()
- injectUserHeaders()
]
rectangle "AuthController\nAuthService" as Auth [
**AuthService**
--
+ login()
+ logout()
+ parseToken()
]
rectangle "UserDetailsServiceImpl" as UDS [
**UserDetailsServiceImpl**
--
+ loadUserByUsername()
]
rectangle "JwtUtil" as JWT [
**JwtUtil**
--
+ createJWT()
+ parseJWT()
]
rectangle "RedisUtil\n(Redis白名单)" as Redis [
**RedisUtil**
--
+ set()
+ get()
+ delete()
]
database "MySQL\n(emiya_oj_auth)" as DB
rectangle "下游微服务\n(Problem/Judge/Blog)" as DS

' === 登录链路 ===
FE -[#purple]> GW : 1: POST /auth/login\n{username, password}
GW -[#purple]> Auth : 2: 白名单放行\n转发登录请求
Auth -[#purple]> UDS : 3: loadUserByUsername()
UDS -[#purple]> DB : 4: SELECT user
Auth -[#purple]> DB : 5: SELECT roles / permissions
Auth -[#purple]> JWT : 6: createJWT(claims)
Auth -[#purple]> Redis : 7: set(token_{userId}, token)
Auth -[#purple]> GW : 8: UserLoginVO {token}
GW -[#purple]> FE : 9: 登录成功

' === 鉴权链路 ===
FE -[#teal]> GW : 10: GET /protected\nHeader: Bearer {token}
GW -[#teal]> JWT : 11: parseJWT(token)
GW -[#teal]> Redis : 12: get(token_{userId})
GW -[#teal]> DS : 13: 注入 X-User-Id/X-User-Name\nX-User-Roles

' === 登出链路 ===
FE -[#red]> GW : 14: POST /auth/logout
GW -[#red]> Auth : 15: 转发登出请求
Auth -[#red]> Redis : 16: delete(token_{userId})
Auth -[#red]> FE : 17: 登出成功

note right of GW
  1. 白名单路径直接放行
  2. 非白名单路径必须
     校验 JWT + Redis 白名单
  3. 认证通过后注入用户上下文
end note

@enduml
```

---

### 3.5 构件图 (Component Diagram)

展示认证授权模块的软件构件、接口及依赖关系。

```plantuml
@startuml Auth-Component
skinparam componentStyle rectangle

package "EmiyaOJ-Gateway :8080" {
  [Spring Cloud\nGateway] as SCG
  [AuthGlobalFilter] as AGF
  [GatewayWhitelist\nProperties] as GWP
  SCG --> AGF
  AGF --> GWP
}

package "EmiyaOJ-Auth :9010" {
  [auth-api] as AA <<api>>
  [auth-service] as AS <<service>>
  [Spring Security\nConfig] as SEC
  [UserDetailsServiceImpl] as UDS
  
  AA --> AS : 实现
  AS --> SEC : 配置
  SEC --> UDS : 认证入口
}

package "EmiyaOJ-Common" {
  [JwtUtil] as JWT
  [RedisUtil] as REDIS
  [ResponseResult] as RR
  [PageDTO/PageVO] as PAGE
  [BaseContext] as CTX
}

database "MySQL\nemiya_oj_auth" as AUTHDB {
  [user]
  [role]
  [permission]
  [user_role]
  [role_permission]
}

database "Redis\n:6379" as REDISDB {
  [Token 白名单\nkey: token_{userId}]
}

database "Nacos\n:8848" as NACOS {
  [服务注册中心]
  [配置中心]
}

rectangle "REST API\nPOST /auth/login" as LoginAPI
rectangle "REST API\nPOST /auth/logout" as LogoutAPI
rectangle "REST API\nGET /auth/user/parse-token" as ParseAPI
rectangle "REST API\n/user/**" as UserAPI
rectangle "REST API\n/role/**" as RoleAPI
rectangle "REST API\n/permission/**" as PermAPI
rectangle "Feign\nAuthFeignClient" as FeignAC
rectangle "JDBC" as JDBC
rectangle "Redis 协议" as RedisProto

' === 构件依赖 ===
SCG --> LoginAPI : 暴露
SCG --> LogoutAPI : 暴露
SCG --> ParseAPI : 暴露
SCG --> UserAPI : 暴露
SCG --> RoleAPI : 暴露
SCG --> PermAPI : 暴露

LoginAPI --> AA
LogoutAPI --> AA
ParseAPI --> AA
UserAPI --> AA
RoleAPI --> AA
PermAPI --> AA

AGF --> JWT : 依赖
AGF --> REDIS : 依赖
AGF --> GWP : 依赖

AS --> JWT : Token生成/解析
AS --> REDIS : 白名单操作
AS --> JDBC : 数据访问
JDBC --> AUTHDB

REDIS --> RedisProto
RedisProto --> REDISDB

AS -.-> NACOS : 服务注册
SCG -.-> NACOS : 服务发现

' Feign Client
FeignAC --> ParseAPI : 调用
note right of FeignAC
  供其他微服务调用
  用于 Token 解析
  获取用户权限信息
end note

@enduml
```

---

### 3.6 部署图 (Deployment Diagram)

展示认证链路的物理部署节点与网络拓扑。

```plantuml
@startuml Auth-Deployment
skinparam nodeBorderColor #333333
skinparam nodeBackgroundColor #FAFAFA
skinparam defaultFontSize 11

node "用户浏览器" as Browser {
  component "Vue/React\n管理端前端" as AdminFE
  component "Vue/React\n用户端前端" as UserFE
}

node "Docker 宿主机\n(10.0.0.10)" as DockerHost {
  
  rectangle "Docker Network: emiya-net (bridge)" as Net {
    
    node "Gateway 容器\nemiya-oj-gateway:1.0\nPort: 8080" as GW {
      [Spring Cloud Gateway]
      [AuthGlobalFilter]
    }

    node "Auth 容器\nemiya-oj-auth:1.0\nPort: 9010" as Auth {
      [Auth Service\nSpring Boot]
      [Spring Security]
      [UserDetailsService]
    }

    node "Problem 容器\n:9020" as Problem {
      [Problem Service]
    }

    node "Judge 容器\n:9030" as Judge {
      [Judge Service]
    }

    node "Blog 容器\n:9040" as Blog {
      [Blog Service]
    }

    node "Chat 容器\n:9050" as Chat {
      [Chat Service]
    }

    node "MySQL 容器\nmysql:8.0\nPort: 3306" as MySQL {
      database "emiya_oj_auth"
      database "emiya_oj_problem"
      database "emiya_oj_judge"
      database "emiya_oj_blog"
    }

    node "Redis 容器\nredis:7-alpine\nPort: 6379" as Redis {
      database "Token 白名单"
      database "缓存数据 (可选)"
    }

    node "Nacos 容器\nnacos:v2.5.1\nPort: 8848" as Nacos {
      [服务注册中心]
      [配置中心]
    }
  }
}

node "外部 AI 服务\n(DeepSeek/OpenAI)" as AIService <<外部>>

' === 通信链路 ===
AdminFE -[#blue]-> GW : <<HTTPS>>
UserFE -[#blue]-> GW : <<HTTPS>>

GW -[#green]-> Auth : <<HTTP 路由>>
GW -[#green]-> Problem : <<HTTP 路由>>
GW -[#green]-> Judge : <<HTTP 路由>>
GW -[#green]-> Blog : <<HTTP 路由>>
GW -[#green]-> Chat : <<HTTP 路由>>

GW -[#teal]-> Redis : <<Redis 协议\nToken 验证>>
Auth -[#teal]-> Redis : <<Redis 协议\nToken 管理>>

Auth -[#purple]-> MySQL : <<JDBC\nTCP 3306>>
Problem -[#purple]-> MySQL : <<JDBC>>
Judge -[#purple]-> MySQL : <<JDBC>>
Blog -[#purple]-> MySQL : <<JDBC>>

GW -[#orange]-> Nacos : <<HTTP\n服务发现>>
Auth -[#orange]-> Nacos : <<HTTP\n服务注册>>
Problem -[#orange]-> Nacos : <<HTTP\n服务注册>>
Judge -[#orange]-> Nacos : <<HTTP\n服务注册>>
Blog -[#orange]-> Nacos : <<HTTP\n服务注册>>
Chat -[#orange]-> Nacos : <<HTTP\n服务注册>>

Chat -[#red]-> AIService : <<HTTPS\nAI API Key>>

note right of GW
  网关职责:
  1. 统一入口 (Port 8080)
  2. JWT 解析与校验
  3. Redis 白名单验证
  4. 用户上下文注入
  5. 路由转发到下游服务
end note

note bottom of DockerHost
  部署方式:
  - docker-compose up -d
  - 所有容器共享 emiya-net 网络
  - 环境变量注入:
    NACOS_ADDR, MYSQL_HOST,
    REDIS_HOST, JWT_SECRET
  - 基础镜像: eclipse-temurin:21-jre-alpine
end note

@enduml
```

---

## 附录

### A. 与现有文档交叉引用

| 文档 | 关联说明 |
|------|----------|
| `docs/UML-Diagrams.md` | 现有 Mermaid 格式 UML 图；本文件提供 PlantUML 格式的完整替代 |
| `docs/EmiyaOJ-Cloud需求规格说明书.md` | 用例依据，参与者定义和功能需求 |
| `docs/EmiyaOJ-Cloud概要设计说明书.md` | 微服务架构、公共接口、JWT 设计 |
| `docs/详细设计/EmiyaOJ-Cloud判题提交子模块详细设计说明书.md` | 判题用例的详细设计参考 |
| `docs/详细设计/EmiyaOJ-Cloud认证网关子模块详细设计说明书.md` | 认证用例的详细设计参考 |
| `docs/Judge-Submission-API.md` | 判题提交接口定义 |
| `docs/Blog-API.md` | 博客接口定义 |
| `/memories/repo/EmiyaOJ-Cloud-Architecture.md` | 全系统架构、类、端口、数据表清单 |

### B. PlantUML 渲染方式

| 方式 | 说明 |
|------|------|
| VS Code 插件 | 安装 `PlantUML` 扩展 (jebbs.plantuml)，打开 `.md` 文件预览 |
| 在线渲染 | 复制代码块到 [PlantUML Online Server](https://www.plantuml.com/plantuml/uml/) |
| 命令行 | `java -jar plantuml.jar docs/UML2.0-完整建模.md` 生成 PNG/SVG |
| IDEA 插件 | IntelliJ IDEA Ultimate 自带 PlantUML 支持 |

### C. 图清单

| 序号 | 章节 | 图类型 | 图名 |
|------|------|--------|------|
| 1 | 一 | 用例图 | EmiyaOJ-Cloud 全系统用例图 |
| 2 | 二.1 | 活动图 | 代码提交与自动判题活动图 |
| 3 | 二.2 | 类图 | 判题模块核心类图 |
| 4 | 二.3 | 时序图 | 代码提交与异步判题时序图 |
| 5 | 二.4 | 通信图 | 判题链路通信图 |
| 6 | 二.5 | 构件图 | 判题模块构件图 |
| 7 | 二.6 | 部署图 | 判题链路部署图 |
| 8 | 三.1 | 活动图 | 用户认证与授权访问活动图 |
| 9 | 三.2 | 类图 | 认证授权模块核心类图 |
| 10 | 三.3 | 时序图 | 用户登录 & 请求鉴权时序图 (2张) |
| 11 | 三.4 | 通信图 | 认证鉴权链路通信图 |
| 12 | 三.5 | 构件图 | 认证授权模块构件图 |
| 13 | 三.6 | 部署图 | 认证链路部署图 |

---

> **文档版本**: v1.0  
> **建模工具**: PlantUML  
> **最后更新**: 2026-05-20  
> **建模人**: EmiyaOJ-Cloud 开发小组
