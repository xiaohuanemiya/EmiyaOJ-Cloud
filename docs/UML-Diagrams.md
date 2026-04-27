# EmiyaOJ-Cloud — UML 2.0 完整建模文档

> **项目**: EmiyaOJ-Cloud 在线判题系统  
> **架构**: 微服务 (Spring Cloud + Spring Boot + Nacos + Redis + MySQL)  
> **建模日期**: 2026-04-28

---

## 目录

1. [用例图 (Use Case Diagram)](#1-用例图-use-case-diagram)
2. [领域模型类图 (Domain Model / Class Diagram)](#2-领域模型类图)
3. [微服务架构图 (Microservice Architecture Diagram)](#3-微服务架构图)
4. [ER 图 / 数据库关系图](#4-er-图--数据库关系图)
5. [时序图 (Sequence Diagrams)](#5-时序图)
6. [活动图 (Activity Diagram)](#6-活动图)
7. [组件图 (Component Diagram)](#7-组件图)
8. [部署图 (Deployment Diagram)](#8-部署图)

---

## 1. 用例图 (Use Case Diagram)

```mermaid
graph TB
    subgraph "EmiyaOJ-Cloud 在线判题系统"
        subgraph "认证模块"
            UC1[用户注册]
            UC2[用户登录]
            UC3[Token 验证]
            UC4[角色管理]
            UC5[权限管理]
        end

        subgraph "题目模块"
            UC6[浏览题目列表]
            UC7[查看题目详情]
            UC8[创建/编辑题目]
            UC9[管理测试用例]
            UC10[管理编程语言]
            UC11[管理标签]
        end

        subgraph "判题模块"
            UC12[提交代码]
            UC13[查看提交记录]
            UC14[查看判题结果]
            UC15[管理判题沙箱]
        end

        subgraph "博客模块"
            UC16[发布博客]
            UC17[编辑/删除博客]
            UC18[评论博客]
            UC19[收藏博客]
            UC20[管理博客标签]
        end

        subgraph "AI 助手"
            UC21[发送消息]
            UC22[获取代码建议]
        end
    end

    Visitor((访客)) --> UC6
    Visitor --> UC7
    Visitor --> UC1
    Visitor --> UC2

    User((注册用户)) --> UC6
    User --> UC7
    User --> UC12
    User --> UC13
    User --> UC14
    User --> UC16
    User --> UC17
    User --> UC18
    User --> UC19
    User --> UC21
    User --> UC22

    Admin((管理员)) --> User
    Admin --> UC4
    Admin --> UC5
    Admin --> UC8
    Admin --> UC9
    Admin --> UC10
    Admin --> UC11
    Admin --> UC15
    Admin --> UC20
    Admin --> UC3

    GoJudge((Go-Judge 沙箱)) -.-> UC15
```

---

## 2. 领域模型类图

### 2.1 认证服务领域模型 (Auth)

```mermaid
classDiagram
    class User {
        +Long id
        +String username
        +String password
        +String nickname
        +String email
        +String phone
        +String avatar
        +Integer status
        +Integer deleted
        +LocalDateTime createTime
        +LocalDateTime updateTime
        +Long createBy
        +Long updateBy
    }

    class Role {
        +Long id
        +String roleCode
        +String roleName
        +String description
        +Integer status
        +Integer deleted
        +LocalDateTime createTime
        +LocalDateTime updateTime
    }

    class Permission {
        +Long id
        +Long parentId
        +String permissionCode
        +String permissionName
        +Integer permissionType
        +String path
        +String component
        +String icon
        +Integer sortOrder
        +Integer status
    }

    class UserRole {
        +Long id
        +Long userId
        +Long roleId
    }

    class RolePermission {
        +Long id
        +Long roleId
        +Long permissionId
    }

    User "1" --> "*" UserRole : 拥有
    Role "1" --> "*" UserRole : 被分配
    Role "1" --> "*" RolePermission : 包含
    Permission "1" --> "*" RolePermission : 被授予
    Permission "1" --> "*" Permission : 父子关系
```

### 2.2 题目服务领域模型 (Problem)

```mermaid
classDiagram
    class Problem {
        +Long id
        +String title
        +String description
        +String inputDescription
        +String outputDescription
        +String sampleInput
        +String sampleOutput
        +String hint
        +Integer difficulty
        +Integer timeLimit
        +Integer memoryLimit
        +Integer stackLimit
        +String source
        +Long authorId
        +Integer acceptCount
        +Integer submitCount
        +Integer status
        +Integer deleted
    }

    class TestCase {
        +Long id
        +Long problemId
        +String input
        +String output
        +Integer isSample
        +Integer score
        +Integer sortOrder
        +Integer deleted
    }

    class Language {
        +Long id
        +String name
        +String version
        +String compileCommand
        +String executeCommand
        +String sourceFileExt
        +String executableExt
        +Integer isCompiled
        +Double timeLimitMultiplier
        +Double memoryLimitMultiplier
        +Integer status
    }

    class Tag {
        +Long id
        +String name
        +String description
        +String color
    }

    class ProblemTag {
        +Long id
        +Long problemId
        +Long tagId
    }

    Problem "1" --> "*" TestCase : 包含
    Problem "1" --> "*" ProblemTag : 关联
    Tag "1" --> "*" ProblemTag : 标记
```

### 2.3 判题服务领域模型 (Judge)

```mermaid
classDiagram
    class Submission {
        +Long id
        +Long problemId
        +Long userId
        +Long languageId
        +String code
        +Integer status
        +Integer score
        +Long timeUsed
        +Long memoryUsed
        +String errorMessage
        +String compileMessage
        +Double passRate
        +Integer deleted
        +LocalDateTime createTime
    }

    class SubmissionResult {
        +Long id
        +Long submissionId
        +Long testCaseId
        +String status
        +Integer timeUsed
        +Integer memoryUsed
        +String errorMessage
    }

    class GoJudgeRequest {
        +List~Cmd~ cmd
        +Map pipeMapping
    }

    class GoJudgeResult {
        +String status
        +Integer exitStatus
        +Long time
        +Long memory
        +Long runTime
        +Map files
        +Map fileIds
        +String error
    }

    class MessageEvent {
        +Long id
        +String businessType
        +Long businessId
        +Integer status
        +Integer retryCount
        +Integer maxRetryCount
        +String payload
        +LocalDateTime nextRetryTime
    }

    Submission "1" --> "*" SubmissionResult : 产生
    Submission --> GoJudgeRequest : 生成
    GoJudgeResult --> SubmissionResult : 映射
    Submission --> MessageEvent : 触发
```

### 2.4 博客服务领域模型 (Blog)

```mermaid
classDiagram
    class Blog {
        +Long id
        +Long userId
        +String title
        +String content
        +LocalDateTime createTime
        +LocalDateTime updateTime
        +Integer deleted
    }

    class BlogComment {
        +Long id
        +Long blogId
        +Long userId
        +String content
        +LocalDateTime createTime
        +LocalDateTime updateTime
        +Integer deleted
    }

    class BlogStar {
        +Long id
        +Long userId
        +Long blogId
        +LocalDateTime createTime
        +Integer deleted
    }

    class BlogTag {
        +Long id
        +String tag
        +String desc
    }

    class BlogTagAssociation {
        +Long id
        +Long blogId
        +Long tagId
    }

    class BlogPicture {
        +String url
        +Integer deleted
    }

    class UserBlog {
        +Long userId
        +String username
        +String nickname
        +Integer blogCount
        +Integer starCount
        +LocalDateTime createTime
    }

    Blog "1" --> "*" BlogComment : 评论
    Blog "1" --> "*" BlogStar : 收藏
    Blog "1" --> "*" BlogTagAssociation : 关联
    BlogTag "1" --> "*" BlogTagAssociation : 标记
    Blog "*" --> "1" UserBlog : 统计
```

### 2.5 公共模块 (Common)

```mermaid
classDiagram
    class ResponseResult~T~ {
        +Integer code
        +String message
        +T data
        +success(T data)$ ResponseResult~T~
        +error(String msg)$ ResponseResult~T~
    }

    class PageDTO {
        +Integer pageNum
        +Integer pageSize
    }

    class PageVO~T~ {
        +List~T~ records
        +Long total
        +Long pages
        +Integer pageNum
        +Integer pageSize
    }

    class JwtUtil {
        +createJWT(Map claims)$ String
        +parseJWT(String token)$ Claims
    }

    class RedisUtil {
        +set(String key, Object value)
        +get(String key) Object
        +delete(String key)
        +exists(String key) Boolean
    }

    class BaseContext {
        +ThreadLocal~Long~ currentUserId
    }

    class ResultEnum {
        <<enumeration>>
        SUCCESS
        ERROR
        UNAUTHORIZED
        FORBIDDEN
        NOT_FOUND
    }

    class PermissionTypeEnum {
        <<enumeration>>
        MENU
        BUTTON
        API
    }
```

---

## 3. 微服务架构图

```mermaid
graph TB
    subgraph Frontend["前端层"]
        Client["Web 前端 / 移动端"]
    end

    subgraph GWLayer["网关层 :8080"]
        Gateway["EmiyaOJ-Gateway / Spring Cloud Gateway"]
        AuthFilter["AuthGlobalFilter / JWT解析 + Redis白名单校验"]
        Gateway --> AuthFilter
    end

    subgraph Governance["服务治理"]
        Nacos["Nacos Server :8848 / 服务注册 & 配置中心"]
    end

    subgraph AuthSvc["认证服务 :9010"]
        AuthService["EmiyaOJ-Auth / 登录/鉴权/用户管理"]
        AuthDB[("emiya_oj_auth / MySQL")]
        AuthService --> AuthDB
    end

    subgraph ProblemSvc["题目服务 :9020"]
        ProblemService["EmiyaOJ-Problem / 题目CRUD/测试用例/标签"]
        ProblemDB[("emiya_oj_problem / MySQL")]
        ProblemService --> ProblemDB
    end

    subgraph JudgeSvc["判题服务 :9030"]
        JudgeService["EmiyaOJ-Judge / 代码提交/判题执行"]
        JudgeDB[("emiya_oj_judge / MySQL")]
        JudgeService --> JudgeDB
    end

    subgraph BlogSvc["博客服务 :9040"]
        BlogService["EmiyaOJ-Blog / 博客/评论/收藏"]
        BlogDB[("emiya_oj_blog / MySQL")]
        BlogService --> BlogDB
    end

    subgraph ChatSvc["AI 服务 :9050"]
        ChatService["EmiyaOJ-Chat / AI对话辅助"]
    end

    subgraph Sandbox["判题沙箱"]
        GoJudge["Go-Judge :5050 / 代码编译&执行沙箱"]
    end

    subgraph Cache["缓存"]
        Redis[("Redis :6379 / Token白名单 & 缓存")]
    end

    Client -->|"HTTP"| Gateway
    Gateway -->|"路由转发"| AuthService
    Gateway -->|"路由转发"| ProblemService
    Gateway -->|"路由转发"| JudgeService
    Gateway -->|"路由转发"| BlogService
    Gateway -->|"路由转发"| ChatService

    AuthService -.->|"服务注册"| Nacos
    ProblemService -.->|"服务注册"| Nacos
    JudgeService -.->|"服务注册"| Nacos
    BlogService -.->|"服务注册"| Nacos
    ChatService -.->|"服务注册"| Nacos
    Gateway -.->|"服务发现"| Nacos

    AuthService -->|"验证Token"| Redis
    Gateway -->|"验证白名单"| Redis

    JudgeService -->|"Feign: 获取题目信息"| ProblemService
    JudgeService -->|"HTTP: 编译 & 运行"| GoJudge
    JudgeService -->|"Feign: 获取用户信息"| AuthService
```

---

## 4. ER 图 / 数据库关系图

### 4.1 认证数据库 (emiya_oj_auth)

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar username UK
        varchar password
        varchar nickname
        varchar email UK
        varchar phone
        varchar avatar
        tinyint status
        tinyint deleted
        datetime create_time
        datetime update_time
    }

    ROLE {
        bigint id PK
        varchar role_code UK
        varchar role_name
        varchar description
        tinyint status
        tinyint deleted
        datetime create_time
    }

    PERMISSION {
        bigint id PK
        bigint parent_id
        varchar permission_code UK
        varchar permission_name
        tinyint permission_type
        varchar path
        varchar component
        varchar icon
        int sort_order
        tinyint status
    }

    USER_ROLE {
        bigint id PK
        bigint user_id FK
        bigint role_id FK
    }

    ROLE_PERMISSION {
        bigint id PK
        bigint role_id FK
        bigint permission_id FK
    }

    USER ||--o{ USER_ROLE : "1:N"
    ROLE ||--o{ USER_ROLE : "1:N"
    ROLE ||--o{ ROLE_PERMISSION : "1:N"
    PERMISSION ||--o{ ROLE_PERMISSION : "1:N"
    PERMISSION ||--o{ PERMISSION : "parent_id (自引用)"
```

### 4.2 题目数据库 (emiya_oj_problem)

```mermaid
erDiagram
    PROBLEM {
        bigint id PK
        varchar title
        text description
        text input_description
        text output_description
        text sample_input
        text sample_output
        text hint
        tinyint difficulty
        int time_limit
        int memory_limit
        int stack_limit
        varchar source
        bigint author_id
        int accept_count
        int submit_count
        tinyint status
        tinyint deleted
    }

    TEST_CASE {
        bigint id PK
        bigint problem_id FK
        longtext input
        longtext output
        tinyint is_sample
        int score
        int sort_order
        tinyint deleted
    }

    LANGUAGE {
        bigint id PK
        varchar name
        varchar version
        varchar compile_command
        varchar execute_command
        varchar source_file_ext
        varchar executable_ext
        tinyint is_compiled
        double time_limit_multiplier
        double memory_limit_multiplier
        tinyint status
    }

    TAG {
        bigint id PK
        varchar name UK
        varchar description
        varchar color
    }

    PROBLEM_TAG {
        bigint id PK
        bigint problem_id FK
        bigint tag_id FK
    }

    PROBLEM ||--o{ TEST_CASE : "1:N"
    PROBLEM ||--o{ PROBLEM_TAG : "1:N"
    TAG ||--o{ PROBLEM_TAG : "1:N"
```

### 4.3 判题数据库 (emiya_oj_judge)

```mermaid
erDiagram
    SUBMISSION {
        bigint id PK
        bigint problem_id
        bigint user_id
        bigint language_id
        longtext code
        int status
        int score
        bigint time_used
        bigint memory_used
        text error_message
        text compile_message
        double pass_rate
        tinyint deleted
        datetime create_time
    }

    SUBMISSION_RESULT {
        bigint id PK
        bigint submission_id FK
        bigint test_case_id
        varchar status
        int time_used
        int memory_used
        text error_message
    }

    MESSAGE_EVENT {
        bigint id PK
        varchar business_type
        bigint business_id
        int status
        int retry_count
        int max_retry_count
        text payload
        datetime next_retry_time
    }

    SUBMISSION ||--o{ SUBMISSION_RESULT : "1:N"
    SUBMISSION ||--o{ MESSAGE_EVENT : "关联"
```

### 4.4 博客数据库 (emiya_oj_blog)

```mermaid
erDiagram
    BLOG {
        bigint id PK
        bigint user_id
        varchar title
        text content
        datetime create_time
        datetime update_time
        tinyint deleted
    }

    BLOG_COMMENT {
        bigint id PK
        bigint blog_id FK
        bigint user_id
        text content
        datetime create_time
        datetime update_time
        tinyint deleted
    }

    BLOG_STAR {
        bigint id PK
        bigint user_id
        bigint blog_id FK
        datetime create_time
        tinyint deleted
    }

    BLOG_TAG {
        bigint id PK
        varchar tag
        varchar desc
    }

    BLOG_TAG_ASSOCIATION {
        bigint id PK
        bigint blog_id FK
        bigint tag_id FK
    }

    BLOG_PICTURE {
        varchar url PK
        tinyint deleted
    }

    USER_BLOG {
        bigint user_id PK
        varchar username
        varchar nickname
        int blog_count
        int star_count
        datetime create_time
    }

    BLOG ||--o{ BLOG_COMMENT : "1:N"
    BLOG ||--o{ BLOG_STAR : "1:N"
    BLOG ||--o{ BLOG_TAG_ASSOCIATION : "1:N"
    BLOG_TAG ||--o{ BLOG_TAG_ASSOCIATION : "1:N"
```

---

## 5. 时序图

### 5.1 用户登录与 Token 验证

```mermaid
sequenceDiagram
    actor User as 用户
    participant GW as API Gateway
    participant Auth as Auth Service
    participant Redis as Redis
    participant DB as Auth DB

    User->>GW: POST /auth/login (username, password)
    GW->>Auth: 转发登录请求
    Auth->>DB: 查询用户 (username)
    DB-->>Auth: 返回用户信息
    Auth->>Auth: 验证密码 (BCrypt)
    alt 密码错误
        Auth-->>GW: 401 用户名或密码错误
        GW-->>User: 登录失败
    else 密码正确
        Auth->>Auth: 生成 JWT Token (HMAC-SHA256)
        Auth->>Redis: 写入白名单 token_{userId}
        Auth-->>GW: 200 Token + 用户信息
        GW-->>User: 登录成功
    end

    Note over User,Redis: 后续请求流程
    User->>GW: GET /problem/list (Header: Authorization Bearer xxx)
    GW->>GW: AuthGlobalFilter 解析 Token
    GW->>GW: 提取 userId 和 roles
    GW->>Redis: 验证 token_{userId} 存在
    Redis-->>GW: 存在
    GW->>GW: 注入 X-User-Id, X-User-Roles
    GW->>Problem: 转发请求 (带用户上下文头)
```

### 5.2 代码提交与判题执行

```mermaid
sequenceDiagram
    actor User as 用户
    participant GW as Gateway
    participant Judge as Judge Service
    participant DB as Judge DB
    participant Problem as Problem Service (Feign)
    participant GoJudge as Go-Judge 沙箱
    participant Msg as 本地消息表

    User->>GW: POST /judge/submit (problemId, languageId, code)
    GW->>Judge: 转发 (X-User-Id)
    Judge->>DB: 创建 Submission (status=PENDING)
    DB-->>Judge: submissionId
    Judge-->>User: 200 {submissionId}

    Note over Judge: @Async 异步判题开始

    Judge->>DB: 更新 status=JUDGING
    Judge->>Problem: Feign: getProblemById()
    Problem-->>Judge: Problem 信息
    Judge->>Problem: Feign: getLanguageById()
    Problem-->>Judge: Language 信息
    Judge->>Problem: Feign: getTestCasesByProblemId()
    Problem-->>Judge: List~TestCase~

    alt 需要编译
        Judge->>GoJudge: HTTP: /run (编译命令)
        GoJudge-->>Judge: 编译结果
        alt 编译失败
            Judge->>DB: 更新 status=CE
            Judge->>Msg: 写入消息 (COMPILE_ERROR)
        end
    end

    loop 每个测试用例
        Judge->>GoJudge: HTTP: /run (执行命令 + stdin)
        GoJudge-->>Judge: GoJudgeResult (status, time, memory, output)
        Judge->>Judge: 比对 output vs 预期输出
        Judge->>DB: 保存 SubmissionResult
    end

    Judge->>Judge: 计算通过率 & 得分
    Judge->>DB: 更新 Submission (status=AC/WA/...)
    Judge->>Msg: 写入消息 (JUDGE_COMPLETE)
```

### 5.3 博客发布与评论

```mermaid
sequenceDiagram
    actor Author as 作者
    actor Reader as 读者
    participant GW as Gateway
    participant Blog as Blog Service
    participant BlogDB as Blog DB
    participant Auth as Auth Service (Feign)

    Author->>GW: POST /blog/save (title, content)
    GW->>Blog: 转发 (X-User-Id)
    Blog->>BlogDB: INSERT blog
    Blog->>BlogDB: INSERT blog_tag_association
    Blog->>BlogDB: MySQL Trigger → UPDATE user_blog (blogCount++)
    Blog-->>GW: 200 博客发布成功
    GW-->>Author: 博客已发布

    Reader->>GW: POST /blog/{blogId}/comment (content)
    GW->>Blog: 转发 (X-User-Id)
    Blog->>BlogDB: INSERT blog_comment
    Blog-->>GW: 200 评论成功
    GW-->>Reader: 评论已发布

    Reader->>GW: POST /blog/{blogId}/star
    GW->>Blog: 转发 (X-User-Id)
    Blog->>BlogDB: INSERT blog_star
    Blog->>BlogDB: UPDATE user_blog (starCount++)
    Blog-->>GW: 200 收藏成功
    GW-->>Reader: 已收藏
```

---

## 6. 活动图

### 6.1 判题执行活动图

```mermaid
stateDiagram-v2
    [*] --> 接收提交请求
    接收提交请求 --> 创建Submission记录
    创建Submission记录 --> 状态_PENDING
    状态_PENDING --> 状态_JUDGING : @Async 异步执行
    状态_JUDGING --> 获取题目信息 : Feign调用ProblemService
    获取题目信息 --> 获取编程语言配置
    获取编程语言配置 --> 获取测试用例列表
    获取测试用例列表 --> 检查是否需要编译
    
    检查是否需要编译 --> 编译代码 : 需要编译
    检查是否需要编译 --> 运行测试用例 : 解释型语言
    
    编译代码 --> 编译成功检查
    编译成功检查 --> 状态_CE : 编译失败
    编译成功检查 --> 运行测试用例 : 编译成功
    
    运行测试用例 --> 比对输出结果
    比对输出结果 --> 记录SubmissionResult
    
    记录SubmissionResult --> 还有更多用例检查
    还有更多用例检查 --> 运行测试用例 : 是
    还有更多用例检查 --> 计算通过率 : 否
    
    计算通过率 --> 判定最终状态
    
    判定最终状态 --> 状态_AC : 全部通过
    判定最终状态 --> 状态_WA : 答案错误
    判定最终状态 --> 状态_TLE : 超时
    判定最终状态 --> 状态_MLE : 内存超限
    判定最终状态 --> 状态_RE : 运行错误
    判定最终状态 --> 状态_PA : 部分通过
    
    状态_AC --> 写入本地消息表
    状态_WA --> 写入本地消息表
    状态_TLE --> 写入本地消息表
    状态_MLE --> 写入本地消息表
    状态_RE --> 写入本地消息表
    状态_PA --> 写入本地消息表
    状态_CE --> 写入本地消息表
    写入本地消息表 --> [*]
```

### 6.2 用户认证活动图

```mermaid
stateDiagram-v2
    [*] --> 用户访问系统
    用户访问系统 --> Gateway拦截
    Gateway拦截 --> 白名单检查
    
    白名单检查 --> 直接放行 : 在白名单中
    白名单检查 --> 提取Token : 需认证
    
    提取Token --> Token存在检查
    Token存在检查 --> 返回401 : 无Token
    Token存在检查 --> 解析JWT : 有Token
    
    解析JWT --> JWT有效检查
    JWT有效检查 --> 返回401 : JWT无效/过期
    JWT有效检查 --> 验证Redis白名单 : JWT有效
    
    验证Redis白名单 --> 返回401 : 不在白名单
    验证Redis白名单 --> 注入请求头 : 在白名单
    
    注入请求头 --> 路由转发
    直接放行 --> 路由转发
    路由转发 --> 微服务处理
    微服务处理 --> 返回响应
    返回响应 --> [*]
```

---

## 7. 组件图

```mermaid
graph TB
    subgraph Gateway["EmiyaOJ-Gateway 网关"]
        GW_Filter["AuthGlobalFilter 全局认证过滤器"]
        GW_Route["路由规则配置"]
        GW_Config["GatewayConfig 白名单/JWT属性配置"]
        GW_Filter --> GW_Route
        GW_Config --> GW_Filter
    end

    subgraph Auth["EmiyaOJ-Auth 认证服务"]
        Auth_Ctrl["AuthController 登录/登出"]
        User_Ctrl["UserController 用户CRUD"]
        Role_Ctrl["RoleController 角色管理"]
        Perm_Ctrl["PermissionController 权限管理"]
        Auth_Svc["AuthService"]
        UserDetails_Svc["UserDetailsServiceImpl"]
        Auth_Feign["AuthFeignClient"]
        Auth_Entities["实体: User, Role, Permission"]
        Auth_Mapper["MyBatis-Plus Mapper"]
        Auth_Ctrl --> Auth_Svc
        User_Ctrl --> UserDetails_Svc
        Role_Ctrl --> Auth_Mapper
        Perm_Ctrl --> Auth_Mapper
        Auth_Svc --> Auth_Mapper
        Auth_Feign --> Auth_Svc
    end

    subgraph Problem["EmiyaOJ-Problem 题目服务"]
        Prob_Ctrl["ProblemController"]
        TC_Ctrl["TestCaseController"]
        Lang_Ctrl["LanguageController"]
        Prob_Svc["ProblemService"]
        TC_Svc["TestCaseService"]
        Lang_Svc["LanguageService"]
        Prob_Feign["ProblemFeignClient"]
        Prob_Mapper["MyBatis-Plus Mapper"]
        Prob_Ctrl --> Prob_Svc
        TC_Ctrl --> TC_Svc
        Lang_Ctrl --> Lang_Svc
        Prob_Feign --> Prob_Svc
        Prob_Feign --> TC_Svc
        Prob_Feign --> Lang_Svc
    end

    subgraph Judge["EmiyaOJ-Judge 判题服务"]
        Judge_Ctrl["JudgeController"]
        Sub_Ctrl["SubmissionController"]
        Sub_Svc["SubmissionService"]
        Judge_Exec["JudgeExecutor @Async"]
        GoJudge_Svc["GoJudgeService HTTP客户端"]
        Judge_Msg["MessageEvent 本地消息表"]
        Judge_Ctrl --> Sub_Svc
        Sub_Ctrl --> Sub_Svc
        Sub_Svc --> Judge_Exec
        Judge_Exec --> GoJudge_Svc
        Judge_Exec --> Judge_Msg
    end

    subgraph Blog["EmiyaOJ-Blog 博客服务"]
        Blog_Ctrl["BlogController"]
        Blog_Svc["IBlogService"]
        UserBlog_Svc["IUserBlogService"]
        Blog_Feign["BlogFeignClient"]
        Blog_Mapper["MyBatis-Plus Mapper"]
        Blog_Ctrl --> Blog_Svc
        Blog_Ctrl --> UserBlog_Svc
        Blog_Feign --> Blog_Svc
    end

    subgraph Chat["EmiyaOJ-Chat AI服务"]
        Chat_Ctrl["ChatController"]
        Chat_Svc["IChatService"]
        Chat_Feign["ChatFeignClient"]
        Chat_Config["ChatServiceConfig"]
        Chat_Ctrl --> Chat_Svc
        Chat_Svc --> Chat_Config
    end

    subgraph Common["EmiyaOJ-Common 公共模块"]
        Com_Result["ResponseResult T"]
        Com_Page["PageDTO / PageVO T"]
        Com_JWT["JwtUtil HMAC-SHA256"]
        Com_Redis["RedisUtil"]
        Com_Context["BaseContext ThreadLocal"]
        Com_Handler["GlobalExceptionHandler"]
        Com_Config["MybatisPlus/Jackson/Feign/Redis Config"]
    end

    Auth_Ctrl -.->|"依赖"| Com_Result
    Prob_Ctrl -.->|"依赖"| Com_Result
    Judge_Ctrl -.->|"依赖"| Com_Result
    Blog_Ctrl -.->|"依赖"| Com_Result
    Chat_Ctrl -.->|"依赖"| Com_Result
```

---

## 8. 部署图

```mermaid
graph TB
    subgraph DockerHost["Docker Host"]
        subgraph GWLayer["网关层"]
            GW_Container["EmiyaOJ-Gateway :8080 / eclipse-temurin:21-jre-alpine"]
        end

        subgraph SvcLayer["微服务层"]
            Auth_Container["EmiyaOJ-Auth :9010"]
            Problem_Container["EmiyaOJ-Problem :9020"]
            Judge_Container["EmiyaOJ-Judge :9030"]
            Blog_Container["EmiyaOJ-Blog :9040"]
            Chat_Container["EmiyaOJ-Chat :9050"]
        end

        subgraph InfraLayer["基础设施层"]
            Nacos_Container["Nacos Server :8848"]
            Redis_Container["Redis :6379"]
            MySQL_Container["MySQL 8.0 :3306"]
        end

        subgraph SandboxLayer["沙箱层"]
            GoJudge_Container["Go-Judge :5050 / 独立沙箱环境"]
        end
    end

    subgraph Storage["持久化存储"]
        MySQL_Vol["(MySQL Data Volume)"]
        Redis_Vol["(Redis Data Volume)"]
        Nacos_Vol["(Nacos Data Volume)"]
    end

    GW_Container -->|"路由"| Auth_Container
    GW_Container -->|"路由"| Problem_Container
    GW_Container -->|"路由"| Judge_Container
    GW_Container -->|"路由"| Blog_Container
    GW_Container -->|"路由"| Chat_Container

    Auth_Container -->|"读写"| Redis_Container
    GW_Container -->|"验证白名单"| Redis_Container

    Auth_Container -->|"读写"| MySQL_Container
    Problem_Container -->|"读写"| MySQL_Container
    Judge_Container -->|"读写"| MySQL_Container
    Blog_Container -->|"读写"| MySQL_Container

    Judge_Container -->|"HTTP 编译执行"| GoJudge_Container
    Judge_Container -.->|"Feign 获取数据"| Problem_Container
    Judge_Container -.->|"Feign 获取用户"| Auth_Container

    MySQL_Container --> MySQL_Vol
    Redis_Container --> Redis_Vol
    Nacos_Container --> Nacos_Vol

    Auth_Container -.->|"注册"| Nacos_Container
    Problem_Container -.->|"注册"| Nacos_Container
    Judge_Container -.->|"注册"| Nacos_Container
    Blog_Container -.->|"注册"| Nacos_Container
    Chat_Container -.->|"注册"| Nacos_Container
    GW_Container -.->|"发现"| Nacos_Container
```

---

## 附录：判题状态码速查

| 状态码 | 枚举值 | 含义 |
|--------|--------|------|
| 0 | PENDING | 待判题 |
| 1 | JUDGING | 判题中 |
| 2 | AC (Accepted) | 通过 |
| 3 | CE (Compile Error) | 编译错误 |
| 4 | SE (System Error) | 系统错误 |
| 5 | WA (Wrong Answer) | 答案错误 |
| 6 | TLE (Time Limit Exceeded) | 时间超限 |
| 7 | MLE (Memory Limit Exceeded) | 内存超限 |
| 8 | RE (Runtime Error) | 运行错误 |
| 9 | OLE (Output Limit Exceeded) | 输出超限 |
| 10 | PA (Partially Accepted) | 部分通过 |

---

> **建模工具**: Mermaid.js | **文档版本**: v1.0 | **作者**: GitHub Copilot
