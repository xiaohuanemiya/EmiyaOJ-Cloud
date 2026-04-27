# EmiyaOJ-Cloud 深度技术分析报告

> **分析日期**: 2026-04-28 | **项目版本**: 1.0-SNAPSHOT | **分析工具**: DeepSeek V4 Pro

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈全景图](#2-技术栈全景图)
3. [微服务架构设计](#3-微服务架构设计)
4. [JWT 认证与安全体系](#4-jwt-认证与安全体系)
5. [Nacos 服务注册与发现](#5-nacos-服务注册与发现)
6. [API 网关架构](#6-api-网关架构)
7. [Feign 微服务间通信](#7-feign-微服务间通信)
8. [异步判题引擎](#8-异步判题引擎)
9. [Go-Judge 沙箱集成](#9-go-judge-沙箱集成)
10. [数据库设计与 MyBatis-Plus](#10-数据库设计与-mybatis-plus)
11. [Redis 缓存策略](#11-redis-缓存策略)
12. [Docker 容器化部署](#12-docker-容器化部署)
13. [SpringDoc OpenAPI 文档](#13-springdoc-openapi-文档)
14. [统一异常处理与响应封装](#14-统一异常处理与响应封装)
15. [权限管理 RBAC 模型](#15-权限管理-rbac-模型)
16. [AI 聊天服务集成](#16-ai-聊天服务集成)
17. [应用场景总结](#17-应用场景总结)

---

## 1. 项目概述

### 1.1 项目简介

**EmiyaOJ-Cloud** 是一个基于 **Spring Cloud 微服务架构**的在线编程判题系统（Online Judge），支持用户在线提交代码、自动编译运行、判题打分，同时集成了博客社区和 AI 辅助编程功能。

### 1.2 核心能力

| 能力域 | 描述 |
|--------|------|
| **在线判题** | 支持 C/C++、Java、Python、Go 等语言，自动编译执行并返回 AC/WA/TLE 等 11 种判题状态 |
| **用户认证** | JWT + Redis 白名单双因子认证，网关层统一鉴权 |
| **题目管理** | 题目 CRUD、测试用例管理、标签分类、难度分级 |
| **博客社区** | 博客发布/评论/收藏、用户统计（MySQL Trigger 自动维护） |
| **AI 助手** | 集成通义千问（qwen-turbo），支持代码问题辅助 |
| **RBAC 权限** | 用户 → 角色 → 权限三层模型，支持菜单/按钮/API 三级权限控制 |

### 1.3 架构概览

```
┌──────────────┐     ┌─────────────────────────────────────────────┐
│  Web 前端     │────▶│  EmiyaOJ-Gateway (:8080)                    │
│  / 移动端     │     │  AuthGlobalFilter → JWT解析 + Redis白名单     │
└──────────────┘     └──────┬──────┬──────┬──────┬──────┬──────────┘
                            │      │      │      │      │
              ┌─────────────┤      │      │      │      │
              ▼             ▼      ▼      ▼      ▼      ▼
         Auth(:9010)  Problem(:9020) Judge(:9030) Blog(:9040) Chat(:9050)
              │             │      │      │      │
              ▼             ▼      ▼      ▼      ▼
         emiya_oj_auth  emiya_oj_problem  emiya_oj_judge  emiya_oj_blog
              │             │      │      │
              └─────────────┴──────┴──────┘
                            │
                     MySQL 8.0 (:3306)
                     Redis 7 (:6379)
                     Nacos 2.5.1 (:8848)
                     Go-Judge (:5050)
```

---

## 2. 技术栈全景图

### 2.1 核心依赖矩阵

| 技术 | 版本 | 用途 | 应用场景 |
|------|------|------|----------|
| **Java** | 21 | 运行环境 | LTS 版本，虚拟线程支持 |
| **Spring Boot** | 3.5.5 | 基础框架 | 自动配置、起步依赖、Actuator |
| **Spring Cloud** | 2025.0.0 | 微服务治理 | 服务发现、负载均衡、配置中心 |
| **Spring Cloud Alibaba** | 2025.0.0.0 | 阿里云集成 | Nacos 注册/配置、Sentinel 限流 |
| **Spring Cloud Gateway** | — | API 网关 | 统一入口、路由转发、全局过滤器 |
| **Spring Security** | — | 安全框架 | BCrypt 密码加密、认证管理器 |
| **Nacos** | 2.5.1 | 服务注册/配置 | 服务发现、动态配置刷新 |
| **MyBatis-Plus** | 3.5.16 | ORM 框架 | Lambda 查询、分页插件、逻辑删除 |
| **MySQL** | 8.0.31 | 关系数据库 | 分库存储（4 个独立 database） |
| **Redis** | 7-alpine | 缓存/白名单 | Token 白名单、会话管理 |
| **JJWT** | 0.12.6 | JWT 令牌 | HMAC-SHA256 签名、Claims 解析 |
| **OpenFeign** | — | 服务间调用 | 声明式 REST 客户端 |
| **SpringDoc OpenAPI** | 2.8.6 | API 文档 | Swagger UI，分组文档 |
| **Docker** | — | 容器化 | Docker Compose 一键部署 |
| **Go-Judge** | latest | 判题沙箱 | 隔离编译执行，资源限制 |
| **WebClient** | — | 响应式 HTTP | 调用 Go-Judge REST API |

### 2.2 Maven 模块结构

```
EmiyaOJ-Cloud (父 POM, pom 类型)
├── EmiyaOJ-Common         公共模块（工具类、统一响应、全局异常）
├── EmiyaOJ-Gateway        Spring Cloud Gateway 网关
├── EmiyaOJ-Auth           
│   ├── auth-api           认证服务 API（Feign 接口定义）
│   ├── auth-dto           认证服务 DTO（数据传输对象）
│   └── auth-service       认证服务实现（Spring Security + JWT）
├── EmiyaOJ-Problem
│   ├── problem-api        题目服务 API
│   ├── problem-dto        题目服务 DTO
│   └── problem-service    题目服务实现
├── EmiyaOJ-Judge
│   ├── judge-api          判题服务 API
│   ├── judge-dto          判题服务 DTO
│   └── judge-service      判题服务实现（异步判题 + Go-Judge 集成）
├── EmiyaOJ-Blog
│   ├── blog-api           博客服务 API
│   ├── blog-dto           博客服务 DTO
│   └── blog-service       博客服务实现
└── EmiyaOJ-Chat
    ├── chat-api           聊天服务 API
    ├── chat-dto           聊天服务 DTO
    └── chat-service       聊天服务实现（通义千问集成）
```

> **设计亮点**：每个微服务采用 **api-dto-service 三层 Maven 模块**拆分，api 模块定义 Feign 接口供其他服务依赖，dto 模块定义数据传输对象，service 模块实现具体业务逻辑。实现了接口与实现的彻底分离。

---

## 3. 微服务架构设计

### 3.1 服务职责与端口

| 服务名 | 端口 | 数据库 | 核心职责 |
|--------|------|--------|----------|
| **Gateway** | 8080 | 无 | 统一入口、JWT 鉴权、路由转发、CORS |
| **Auth-Service** | 9010 | emiya_oj_auth | 登录/登出、Token 解析、用户/角色/权限 CRUD |
| **Problem-Service** | 9020 | emiya_oj_problem | 题目 CRUD、测试用例管理、语言配置、标签管理 |
| **Judge-Service** | 9030 | emiya_oj_judge | 代码提交、异步判题、结果记录、本地消息表 |
| **Blog-Service** | 9040 | emiya_oj_blog | 博客 CRUD、评论、收藏、标签、用户统计 |
| **Chat-Service** | 9050 | 无 | AI 对话、代码问题辅助 |
| **Go-Judge** | 5050 | 无 | 代码编译执行沙箱（独立 Go 服务） |

### 3.2 服务间调用关系

```
Judge-Service ──(Feign)──▶ Problem-Service   获取题目、测试用例、语言配置
Judge-Service ──(HTTP)──▶ Go-Judge          代码编译与执行
Judge-Service ──(Feign)──▶ Auth-Service      获取用户信息（可选）
Gateway ──(Redis)────────▶ Redis             Token 白名单验证
Auth-Service ──(Redis)───▶ Redis             Token 白名单写入/删除
```

### 3.3 配置管理

所有服务共享统一的 `bootstrap.yaml` 模板：

```yaml
spring:
  application:
    name: {service-name}          # 服务名（用于 Nacos 注册）
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}  # Docker 环境注入
        namespace: ${NACOS_NAMESPACE:}
      config:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:}
        file-extension: yaml
```

> **设计亮点**：通过 `${ENV_VAR:default}` 模式，同一份配置在本地开发和 Docker 环境下通过环境变量切换，无需修改配置文件。

---

## 4. JWT 认证与安全体系

### 4.1 认证流程全景

```
┌──────────────────────────────────────────────────────────────────┐
│                        用户登录流程                                │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  客户端                 Gateway              Auth-Service          │
│    │                      │                      │                │
│    │ POST /auth/login     │                      │                │
│    │──{username,password}─▶│                      │                │
│    │                      │ (白名单直接转发)       │                │
│    │                      │──────────────────────▶│                │
│    │                      │                      │                │
│    │                      │          ① AuthenticationManager      │
│    │                      │             验证用户名密码(Bcrypt)      │
│    │                      │          ② 生成JWT(HMAC-SHA256)        │
│    │                      │             载荷: userId,username,     │
│    │                      │                   permissions          │
│    │                      │          ③ Redis写入白名单             │
│    │                      │             key: token_{userId}        │
│    │                      │             TTL: 7200000ms(2h)         │
│    │                      │                      │                │
│    │                      │◀─────────────────────│                │
│    │◀──200 {token}────────│                      │                │
│    │                      │                      │                │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                       后续请求认证流程                              │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  客户端                 Gateway                  下游微服务         │
│    │                      │                         │              │
│    │ GET /problem/list    │                         │              │
│    │ Authorization:       │                         │              │
│    │ Bearer {token}       │                         │              │
│    │─────────────────────▶│                         │              │
│    │                      │                         │              │
│    │          ① 检查路径是否在白名单                  │              │
│    │          ② 提取 Bearer Token                   │              │
│    │          ③ Jwts.parser() 直接解析 JWT          │              │
│    │             (在网关层解析, 无需调用Auth服务)      │              │
│    │          ④ Redis验证 token_{userId} 是否存在    │              │
│    │          ⑤ 刷新 Token 有效期                    │              │
│    │          ⑥ 注入请求头:                         │              │
│    │             X-User-Id: {userId}                │              │
│    │             X-User-Name: {username}            │              │
│    │             X-User-Roles: {permissions}        │              │
│    │                      │                         │              │
│    │                      │──── 转发(带用户头) ──────▶│              │
│    │                      │                         │              │
│    │                      │             下游通过 @RequestHeader    │
│    │                      │             获取 X-User-Id            │
│    │                      │                         │              │
│    │                      │◀──── 业务响应 ──────────│              │
│    │◀──200 {data}────────│                         │              │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### 4.2 JWT 生成核心代码

```java
// AuthService.login() 中的 JWT 生成逻辑
Map<String, Object> claims = new HashMap<>();
claims.put(JwtClaimsConstant.USER_ID, userId);       // "userId"
claims.put(JwtClaimsConstant.USERNAME, username);     // "username"
claims.put(JwtClaimsConstant.PERMISSIONS, permissions); // "permissions"

String token = JwtUtil.createJWT(
    jwtProperties.getSecretKey(),   // HMAC-SHA256 密钥
    jwtProperties.getTtl(),         // 7200000ms = 2小时
    claims
);

// Redis 白名单
redisUtil.set("token_" + userId, token, jwtProperties.getTtl());
```

### 4.3 Gateway 层 JWT 解析

```java
// AuthGlobalFilter.filter() — 在网关层直接解析 JWT，无需 Feign 调用 Auth 服务
SecretKey key = Keys.hmacShaKeyFor(
    jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)
);
Claims claims = Jwts.parser()
    .verifyWith(key)          // ← HMAC-SHA256 验签
    .build()
    .parseSignedClaims(token)
    .getPayload();

// 提取用户信息
String userId = String.valueOf(claims.get("userId"));
String username = String.valueOf(claims.get("username"));
String permissions = String.join(",", (List<?>) claims.get("permissions"));

// 验证 Redis 白名单
String tokenKey = "token_" + userId;
reactiveRedisTemplate.hasKey(tokenKey)  // ReactiveStringRedisTemplate
    .flatMap(exists -> {
        if (!exists) return unauthorizedResponse(...);
        // 刷新有效期
        return reactiveRedisTemplate.expire(tokenKey, Duration.ofMillis(ttl))
            .then(chain.filter(mutatedExchange));
    });
```

### 4.4 安全配置汇总

| 安全机制 | 实现方式 | 应用场景 |
|----------|----------|----------|
| **密码加密** | BCryptPasswordEncoder | 用户注册/登录时密码不可逆存储 |
| **Token 签名** | JJWT HMAC-SHA256 | 防篡改，网关层独立验签 |
| **Token 白名单** | Redis `token_{userId}` | 主动登出即时失效，无需等待 JWT 过期 |
| **白名单路径** | Gateway `gateway.whitelist` 配置 | `/auth/login`、Swagger 文档等无需认证 |
| **请求头注入** | Gateway 注入 `X-User-*` 头 | 下游服务无需重复解析 Token |
| **登出机制** | 删除 Redis 白名单 key | 即时失效，下次请求网关 401 |
| **自动刷新** | 每次验证时 `expire()` 续期 | 活跃用户无需重新登录 |

### 4.5 应用场景分析

| 场景 | 技术方案 | 为什么这样设计 |
|------|----------|---------------|
| **用户登录** | Spring Security `AuthenticationManager` + BCrypt + JWT | Spring Security 提供成熟的认证链，JWT 无状态便于微服务横向扩展 |
| **Token 存储** | 只在载荷存 userId/username/permissions，不存整个 User 对象 | 减小 Token 体积，避免敏感信息泄露，网关解析更快 |
| **主动登出** | 删除 Redis `token_{userId}` | JWT 本身无法主动失效，Redis 白名单解决了"签发后无法撤销"的固有问题 |
| **网关鉴权** | 网关层直接解析 JWT | 避免每次请求都 Feign 调用 Auth 服务，减少网络开销和延迟 |
| **下游获取用户** | `@RequestHeader("X-User-Id")` | 解耦认证与业务，下游服务不感知 JWT 细节 |

---

## 5. Nacos 服务注册与发现

### 5.1 注册配置

所有微服务启动时自动注册到 Nacos：

```yaml
# bootstrap.yaml（所有服务通用）
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:}  # 可选：多环境隔离
      config:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        file-extension: yaml
```

### 5.2 服务发现应用

Nacos 在以下场景自动提供服务发现：

| 场景 | 技术 | 说明 |
|------|------|------|
| **网关路由** | `lb://service-name` | Spring Cloud Gateway 自动从 Nacos 获取实例列表 |
| **Feign 调用** | `@FeignClient(name="service-name")` | Feign + LoadBalancer 自动负载均衡 |
| **健康检查** | Nacos 心跳机制 | 自动剔除不健康实例 |

### 5.3 Docker Compose 中的 Nacos

```yaml
nacos:
  image: nacos/nacos-server:v2.5.1
  environment:
    MODE: standalone               # 单机模式（生产可切换集群）
    NACOS_AUTH_ENABLE: "false"     # 内网环境无需开启认证
    JVM_XMS: 256m                  # 初始堆内存
    JVM_XMX: 256m                  # 最大堆内存
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/console/health/readiness"]
    interval: 10s
    retries: 10
```

### 5.4 应用场景分析

| 场景 | 为什么选择 Nacos |
|------|-----------------|
| **服务注册与发现** | 相较于 Eureka（已停止维护），Nacos 同时提供配置中心，AP+CP 模式可切换 |
| **配置中心** | 支持配置热刷新（`@RefreshScope`），无需重启服务即可修改配置 |
| **Docker 环境** | 通过 `depends_on` + `healthcheck` 确保启动顺序，`condition: service_healthy` 等待 Nacos 就绪 |
| **命名空间隔离** | 支持 `dev/test/prod` 环境隔离，通过 `${NACOS_NAMESPACE}` 环境变量切换 |

---

## 6. API 网关架构

### 6.1 路由配置

```yaml
spring:
  cloud:
    gateway:
      globalcors:                        # 全局 CORS
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            maxAge: 3600
      routes:
        - id: auth-service
          uri: lb://auth-service         # lb:// = LoadBalancer 负载均衡
          predicates:
            - Path=/auth/**
        - id: problem-service
          uri: lb://problem-service
          predicates:
            - Path=/problem/**,/tag/**,/test-case/**,/language/**
        - id: judge-service
          uri: lb://judge-service
          predicates:
            - Path=/judge/**,/submission/**
        - id: blog-service
          uri: lb://blog-service
          predicates:
            - Path=/blog/**
        - id: chat-service
          uri: lb://chat-service
          predicates:
            - Path=/client/chat/**
        - id: auth-management             # 用户/角色/权限管理也走 Auth 服务
          uri: lb://auth-service
          predicates:
            - Path=/user/**,/role/**,/permission/**
```

### 6.2 AuthGlobalFilter 过滤器链

```
请求进入 → GlobalFilter (Order: -100, 最高优先级)
│
├─ 1. isWhitelisted(path)? ─── 是 → chain.filter() 直接放行
│     (AntPathMatcher 匹配 /auth/login, /swagger-ui/** 等)
│
├─ 2. extractToken() — 从 Authorization 头提取 Bearer Token
│     无 Token → 401 UNAUTHORIZED
│
├─ 3. Jwts.parser().verifyWith(key).parseSignedClaims()
│     解析失败 → 401
│
├─ 4. reactiveRedisTemplate.hasKey("token_" + userId)
│     不存在 → 401 (已登出或过期)
│
├─ 5. reactiveRedisTemplate.expire() 刷新 TTL
│
└─ 6. 注入请求头 → chain.filter(mutatedExchange)
       X-User-Id, X-User-Name, X-User-Roles
```

### 6.3 白名单配置

```yaml
gateway:
  whitelist:
    - /auth/login              # 登录接口
    - /auth/user/parse-token   # Feign 内部 Token 解析
    - /swagger-ui/**           # Swagger UI
    - /swagger-ui.html
    - /v3/api-docs/**          # OpenAPI 规范
```

### 6.4 应用场景分析

| 设计选择 | 理由 |
|----------|------|
| **网关层 JWT 解析** | 避免每次请求都 Feign 调用 Auth 服务，减少 ~50ms 网络延迟 |
| **响应式 Redis** | 使用 `ReactiveStringRedisTemplate` 而非阻塞式 `StringRedisTemplate`，适配 WebFlux 网关 |
| **AntPathMatcher 白名单** | 支持 `/swagger-ui/**` 通配符，比精确匹配更灵活 |
| **401 统一响应** | 网关直接返回 JSON `{"code":401,"message":"..."}` ，不在下游服务分散处理认证异常 |
| **Order=-100** | 确保在 Spring Cloud Gateway 内置过滤器之前执行，未认证请求不会到达下游 |

---

## 7. Feign 微服务间通信

### 7.1 Feign 客户端定义

```java
// ProblemFeignClient — Judge 服务调用 Problem 服务
@FeignClient(name = "problem-service")
public interface ProblemFeignClient {
    
    @GetMapping("/problem/{id}")
    ResponseResult<ProblemVO> getProblemById(@PathVariable("id") Long id);
    
    @GetMapping("/test-case/problem/{problemId}")
    ResponseResult<List<TestCaseVO>> getTestCasesByProblemId(
        @PathVariable("problemId") Long problemId
    );
    
    @GetMapping("/language/{id}")
    ResponseResult<LanguageVO> getLanguageById(@PathVariable("id") Long id);
}

// AuthFeignClient — 其他服务调用 Auth 服务
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthFeignClient {
    @GetMapping("/user/parse-token")
    ResponseResult<UserAuthDTO> parseToken(@RequestParam("token") String token);
}

// BlogFeignClient — 跨服务获取博客
@FeignClient(value = "blog-service", contextId = "blogFeignClient")
public interface BlogFeignClient {
    @GetMapping("/blog/{bid}")
    ResponseResult<BlogVO> getBlogById(@PathVariable("bid") Long bid);
}
```

### 7.2 Feign 调用链路

```
Judge-Service.submitCode()
  │
  ├─ problemFeignClient.getProblemById(problemId)
  │    └─ Nacos 发现 problem-service 实例 → LoadBalancer 选择实例 → HTTP 调用
  │
  ├─ problemFeignClient.getLanguageById(languageId)
  │    └─ 同上
  │
  └─ problemFeignClient.getTestCasesByProblemId(problemId)
       └─ 同上
```

### 7.3 应用场景分析

| 技术特点 | 说明 |
|----------|------|
| **声明式调用** | `@FeignClient` + 接口方法注解，无需手写 HTTP 代码 |
| **负载均衡** | 自动集成 Spring Cloud LoadBalancer，`lb://service-name` |
| **服务发现** | 通过 Nacos 自动获取目标服务实例列表 |
| **api 模块分离** | Feign 接口定义在 `*-api` 模块，调用方只需依赖 api 模块，不依赖 service 实现 |
| **contextId** | 同一服务有多个 FeignClient 时用 `contextId` 区分 |

---

## 8. 异步判题引擎

### 8.1 判题流程全景

```
用户提交代码 (POST /judge/submit)
    │
    ▼
JudgeController.submitCode(SubmitCodeDTO, X-User-Id)
    │
    ▼
SubmissionService.submitCode()
    │
    ├── 1. 创建 Submission 记录 (status=0 PENDING)
    │      立即返回 submissionId 给用户
    │
    └── 2. judgeExecutor.executeJudgeAsync(submissionId, ...)  [@Async 异步]
            │
            ├── 更新 status=1 JUDGING
            │
            ├── Feign: getLanguageById() ─── 获取编译/执行命令
            │
            ├── Feign: getTestCasesByProblemId() ─── 获取输入/预期输出
            │
            ├── Feign: getProblemById() ─── 获取时间/内存限制
            │
            ├── 编译阶段
            │   ├── 解释型语言 (Python): 跳过
            │   └── 编译型语言 (C/C++/Java/Go):
            │       └── goJudgeService.compile() ──▶ POST /run (编译命令)
            │           ├── 成功 → 获取 fileIds (缓存编译产物)
            │           └── 失败 → 更新 status=3 CE, 结束
            │
            ├── 运行阶段 (逐个测试用例)
            │   for each testCase:
            │       goJudgeService.run() ──▶ POST /run (执行命令 + stdin)
            │       ├── 获取 status, time, memory, stdout
            │       ├── 对比 stdout vs 预期输出
            │       └── 保存 SubmissionResult
            │
            └── 汇总阶段
                ├── 计算通过率: passCount / totalCount
                ├── 判定最终状态: AC / WA / TLE / MLE / RE ...
                └── 更新 Submission 最终结果
```

### 8.2 @Async 异步机制

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeExecutor {

    private final SubmissionMapper submissionMapper;
    private final GoJudgeService goJudgeService;
    private final ProblemFeignClient problemFeignClient;

    @Async  // ← Spring 异步代理
    public void executeJudgeAsync(Long submissionId, Long problemId, 
                                   Long languageId, String code) {
        // 异步执行判题逻辑，不阻塞 HTTP 请求线程
    }
}
```

> **需要配合 `@EnableAsync`** 在 Spring Boot 启动类上启用异步支持。

### 8.3 判题状态码完整定义

| 状态码 | 英文名 | 中文含义 | 触发条件 |
|--------|--------|----------|----------|
| 0 | PENDING | 待判题 | 提交后初始状态 |
| 1 | JUDGING | 判题中 | @Async 开始执行 |
| 2 | AC | 通过 | 所有测试用例输出与预期一致 |
| 3 | CE | 编译错误 | Go-Judge 编译返回非 ACCEPTED |
| 4 | SE | 系统错误 | 判题服务内部异常 |
| 5 | WA | 答案错误 | 输出与预期不一致 |
| 6 | TLE | 时间超限 | 实际运行时间 > timeLimit |
| 7 | MLE | 内存超限 | 实际内存使用 > memoryLimit |
| 8 | RE | 运行错误 | 程序非零退出或信号终止 |
| 9 | OLE | 输出超限 | 输出超过限制 |
| 10 | PA | 部分通过 | 部分用例通过，部分失败 |

### 8.4 应用场景分析

| 设计选择 | 理由 |
|----------|------|
| **异步判题** | 判题可能耗时数秒，`@Async` 避免阻塞 Tomcat 线程，用户立即收到 submissionId 后轮询结果 |
| **Feign 获取数据** | 题目、测试用例在 Problem 服务中，Judge 服务通过 Feign 获取，保持数据归属清晰 |
| **Go-Judge 隔离** | 用户代码在独立 Docker 容器中执行，与判题服务物理隔离，即使恶意代码也无法影响主服务 |
| **分步状态更新** | PENDING → JUDGING → AC/WA/... 前端可实时展示判题进度 |

---

## 9. Go-Judge 沙箱集成

### 9.1 架构

```
Judge-Service (Java)                Go-Judge (Go)
┌───────────────────┐              ┌─────────────────────┐
│  GoJudgeService   │──HTTP POST──▶│  /run API (:5050)   │
│  (WebClient)      │              │                     │
│                   │              │  ┌─────────────────┐│
│  编译命令构造      │              │  │ 隔离环境         ││
│  执行命令构造      │              │  │ CPU/内存/进程限制 ││
│  结果解析         │◀─JSON resp───│  │ 文件系统隔离     ││
└───────────────────┘              │  └─────────────────┘│
                                   └─────────────────────┘
```

### 9.2 HTTP API 协议

**请求**:
```
POST http://go-judge:5050/run
Content-Type: application/json

[
  {
    "cmd": [{
      "args": ["/usr/bin/gcc", "main.c", "-o", "main"],
      "env": ["PATH=/usr/bin:/bin"],
      "files": [
        {"content": ""},           // stdin
        {"name": "stdout", "max": 10240},
        {"name": "stderr", "max": 10240}
      ],
      "cpuLimit": 10000000000,     // 10秒(纳秒)
      "memoryLimit": 268435456,    // 256MB(字节)
      "procLimit": 50,
      "copyIn": {
        "main.c": {"content": "#include..."}
      },
      "copyOut": ["stdout", "stderr"],
      "copyOutCached": ["main"]    // 缓存编译产物
    }]
  }
]
```

**响应**:
```json
[
  {
    "status": "Accepted",
    "exitStatus": 0,
    "time": 1500000000,         // 1.5秒(纳秒)
    "memory": 52428800,         // 50MB(字节)
    "runTime": 1500000000,
    "files": {
      "stdout": "Hello World\n",
      "stderr": ""
    },
    "fileIds": {
      "main": "abc123"          // 编译产物ID，后续run复用
    }
  }
]
```

### 9.3 GoJudgeService 实现要点

```java
@Service
@RequiredArgsConstructor
public class GoJudgeService {
    
    @Value("${go-judge.url}")
    private String goJudgeUrl;  // Docker: http://go-judge:5050
    
    private final WebClient webClient = WebClient.create();
    
    public GoJudgeResult compile(String code, String languageName) {
        // 根据语言构建不同的编译 Cmd:
        // C:   gcc main.c -o main
        // C++: g++ main.cpp -o main
        // Java: javac Main.java
        // Go: go build -o main main.go
        // Python: 跳过编译
        
        List<GoJudgeRequest> request = List.of(
            GoJudgeRequest.builder()
                .cmd(List.of(buildCompileCmd(code, languageName)))
                .build()
        );
        
        List<GoJudgeResult> results = webClient.post()
            .uri(goJudgeUrl + "/run")
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(GoJudgeResult.class)
            .collectList()
            .block();
        
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }
}
```

### 9.4 资源限制

| 限制项 | 来源 | 说明 |
|--------|------|------|
| **CPU 时间** | `problem.timeLimit` (毫秒) | 题目配置，纳秒级精度传递给 Go-Judge |
| **内存** | `problem.memoryLimit` (MB) | 题目配置，转换为字节 |
| **栈内存** | `problem.stackLimit` (MB) | 题目配置 |
| **输出大小** | 硬编码 10MB | 防止恶意程序刷屏 |
| **进程数** | 硬编码 50 | 防止 fork 炸弹 |

### 9.5 Docker 配置

```yaml
go-judge:
  build:
    context: ./go-judge
    dockerfile: Dockerfile
  privileged: true          # 需要额外权限以设置 cgroup 资源限制
  shm_size: 256m            # 共享内存（某些语言需要）
```

```dockerfile
# go-judge/Dockerfile
FROM criyle/go-judge:latest
USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \       # gcc, g++
    && rm -rf /var/lib/apt/lists/*
```

### 9.6 应用场景分析

| 设计选择 | 理由 |
|----------|------|
| **独立 Go 服务** | Go 语言天然适合系统编程，cgroup 资源控制更精确 |
| **HTTP API 协议** | 语言无关，Java 通过 WebClient 调用 |
| **privileged 模式** | Go-Judge 需要设置 Linux cgroup 限制 CPU/内存/进程数 |
| **copyOutCached** | 编译产物缓存复用，避免每个测试用例都重新编译 |
| **多语言支持** | 通过 `args` 传递不同编译器/解释器命令，支持 C/C++/Java/Python/Go/JS |

---

## 10. 数据库设计与 MyBatis-Plus

### 10.1 分库策略

| 数据库 | 服务 | 核心表 | 分库理由 |
|--------|------|--------|----------|
| `emiya_oj_auth` | Auth | user, role, permission, user_role, role_permission | 安全隔离，认证数据独立 |
| `emiya_oj_problem` | Problem | problem, test_case, language, tag, problem_tag | 业务独立，题目数据单独管理 |
| `emiya_oj_judge` | Judge | submission, submission_result, message_event | 高频写入，隔离判题负载 |
| `emiya_oj_blog` | Blog | blog, blog_comment, blog_star, blog_tag, user_blog | 业务独立，社区数据 |

### 10.2 MyBatis-Plus 关键配置

```yaml
mybatis-plus:
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # DEBUG 日志
    map-underscore-to-camel-case: true                       # 驼峰映射
  global-config:
    db-config:
      update-strategy: not_null      # 只更新非空字段
      id-type: assign_id             # 雪花算法 ID
      logic-delete-field: deleted    # 逻辑删除字段
      logic-delete-value: 1          # 删除标记值
      logic-not-delete-value: 0      # 未删除标记值
  type-aliases-package: com.emiyaoj.{service}.domain.pojo
  mapper-locations: classpath:/mapper/**/*.xml
```

### 10.3 逻辑删除

所有表通过 `deleted` 字段实现逻辑删除，MyBatis-Plus 自动在 SQL 中添加 `WHERE deleted=0`：

```java
// 自动生成: UPDATE blog SET deleted=1 WHERE id=? AND deleted=0
blogMapper.deleteById(blogId);

// 自动生成: SELECT * FROM blog WHERE deleted=0
blogMapper.selectList(null);
```

### 10.4 雪花算法 ID

```yaml
id-type: assign_id  # 使用 Twitter Snowflake 算法生成分布式唯一 ID
```

> 分布式环境下，各服务独立生成 ID，无需中心化 ID 服务，避免单点瓶颈。

### 10.5 消息事件表（本地消息表模式）

```sql
CREATE TABLE `message_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `business_type` varchar(50) NOT NULL COMMENT '业务类型: JUDGE_SUBMIT',
  `business_id` bigint NOT NULL COMMENT '关联业务ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '0-待处理, 1-处理中, 2-成功, 3-失败',
  `retry_count` int DEFAULT 0,
  `max_retry_count` int DEFAULT 3,
  `payload` text COMMENT '消息内容(JSON)',
  `next_retry_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_status_next_retry` (`status`, `next_retry_time`)
);
```

> 分布式事务：判题完成后写入消息事件表，定时任务扫描重试，实现最终一致性。

### 10.6 应用场景分析

| 场景 | MyBatis-Plus 特性 | 优势 |
|------|-------------------|------|
| **分页查询** | `Page<T>` + `page()` 方法 | 自动 COUNT + 分页，无需手写分页 SQL |
| **逻辑删除** | `@TableLogic` + `deleted` 字段 | 数据可恢复，符合 GDPR 合规要求 |
| **分布式 ID** | `assign_id` 雪花算法 | 无需中心化 ID 生成器，性能高 |
| **自动填充** | `@TableField(fill=...)` | createTime/updateTime 自动维护 |
| **枚举映射** | `MybatisEnumTypeHandler` | 状态枚举自动映射，代码更清晰 |

---

## 11. Redis 缓存策略

### 11.1 Token 白名单

| 属性 | 值 |
|------|-----|
| **Key 格式** | `token_{userId}` |
| **Value** | JWT Token 字符串 |
| **TTL** | 7200000ms (2 小时) |
| **刷新策略** | 每次请求验证时 `expire()` 续期 |
| **删除时机** | 用户主动登出 `logout()` |

### 11.2 RedisUtil 工具类

```java
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;
    
    public void set(String key, String value)           // 永久存储
    public void set(String key, String value, long ttl) // 带过期时间
    public String get(String key)                       // 获取
    public void delete(String key)                      // 删除
    public Boolean hasKey(String key)                   // 存在性检查
    public void expire(String key, long ttl)            // 刷新过期
}
```

### 11.3 Gateway 中的响应式 Redis

```java
// 使用 ReactiveStringRedisTemplate（非阻塞）
private final ReactiveStringRedisTemplate reactiveRedisTemplate;

// 验证白名单
reactiveRedisTemplate.hasKey("token_" + userId)
    .flatMap(exists -> {
        if (!exists) return unauthorizedResponse(...);
        return reactiveRedisTemplate.expire("token_" + userId, Duration.ofMillis(ttl))
            .then(chain.filter(exchange));
    });
```

### 11.4 应用场景分析

| 场景 | 为何使用 Redis |
|------|---------------|
| **Token 白名单** | Redis 的 TTL + 快速读写，适合 Token 生命周期管理 |
| **Gateway 响应式** | `ReactiveStringRedisTemplate` 与 WebFlux 网关的非阻塞模型匹配 |
| **主动登出** | 删除 Redis Key 即时生效，无需等待 JWT 自然过期 |
| **高并发** | Redis 单线程模型 + 内存存储，支持每秒 10 万+ 次验证 |

---

## 12. Docker 容器化部署

### 12.1 完整服务拓扑

```
docker-compose.yml (10 个容器 + 3 个数据卷)

┌────────────────────────────────────────────────────────────┐
│  emiyaoj-network (自定义 bridge 网络)                        │
│                                                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ MySQL :3306 │  │ Redis :6379 │  │ Nacos :8848 │        │
│  │ (8.0.31)    │  │ (7-alpine)  │  │ (v2.5.1)    │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
│         │                │                │               │
│  ┌──────┴────────────────┴────────────────┴──────┐        │
│  │              Gateway :8080                      │        │
│  └──────┬──────┬──────┬──────┬──────┬────────────┘        │
│         │      │      │      │      │                      │
│    ┌────▼──┐ ┌▼────┐ ┌▼────┐ ┌▼────┐ ┌▼────┐             │
│    │Auth   │ │Prob │ │Judge│ │Blog │ │Chat │             │
│    │:9010  │ │:9020│ │:9030│ │:9040│ │:9050│             │
│    └───────┘ └─────┘ └──┬───┘ └─────┘ └─────┘             │
│                         │                                  │
│                    ┌────▼─────┐                            │
│                    │ Go-Judge │                            │
│                    │ :5050    │                            │
│                    └──────────┘                            │
└────────────────────────────────────────────────────────────┘

数据卷:
  mysql-data:/var/lib/mysql     (数据库持久化)
  redis-data:/data              (Redis 持久化)
  nacos-data: 未挂载             (Nacos 内存/内嵌 Derby 存储)
```

### 12.2 依赖与启动顺序

```yaml
# Gateway 等待 Nacos + Redis 就绪
gateway:
  depends_on:
    nacos:
      condition: service_healthy    # 健康检查通过
    redis:
      condition: service_healthy

# Auth 等待 MySQL + Nacos + Redis
auth-service:
  depends_on:
    mysql:
      condition: service_healthy
    nacos:
      condition: service_healthy
    redis:
      condition: service_healthy

# Judge 还额外等待 Go-Judge
judge-service:
  depends_on:
    go-judge:
      condition: service_started    # 启动即可，无需健康检查
```

### 12.3 Java 服务 Dockerfile 模板

```dockerfile
FROM eclipse-temurin:21-jre-alpine    # 轻量 JRE 镜像 (~180MB)
LABEL maintainer="EmiyaOJ"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE {port}

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 12.4 环境变量注入

所有服务通过 Docker Compose `environment` 注入配置：

```yaml
environment:
  NACOS_ADDR: nacos:8848       # Docker 内部 DNS 解析
  MYSQL_HOST: mysql
  MYSQL_USER: root
  MYSQL_PASSWORD: root
  REDIS_HOST: redis
  REDIS_PORT: 6379
  GO_JUDGE_URL: http://go-judge:5050   # Judge 专用
```

### 12.5 应用场景分析

| 设计选择 | 理由 |
|----------|------|
| **自定义网络** | `emiyaoj-network` 桥接网络，容器间通过服务名 DNS 通信 |
| **healthcheck** | 确保 MySQL/Nacos 完全就绪后才启动依赖服务，避免连接失败 |
| **数据卷** | MySQL/Redis 数据持久化，容器重启不丢失数据 |
| **Alpine 镜像** | `eclipse-temurin:21-jre-alpine` 仅 ~180MB，远小于完整 JDK 镜像 |
| **SQL 初始化** | `./sql:/docker-entrypoint-initdb.d` 自动执行建库脚本 |

---

## 13. SpringDoc OpenAPI 文档

### 13.1 配置

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
  group-configs:
    - group: default
      display-name: 认证服务
      paths-to-match: '/**'
      packages-to-scan: com.emiyaoj.auth.controller
```

### 13.2 多服务文档聚合

通过 Gateway 统一访问各服务文档：

```
http://localhost:8080/auth/swagger-ui.html        → Auth 服务文档
http://localhost:8080/problem/swagger-ui.html     → Problem 服务文档
http://localhost:8080/judge/swagger-ui.html       → Judge 服务文档
```

> Gateway 白名单已配置 `/swagger-ui/**` 和 `/v3/api-docs/**`，无需认证即可访问。

---

## 14. 统一异常处理与响应封装

### 14.1 ResponseResult 统一响应

```java
@Data
public class ResponseResult<T> implements Serializable {
    private int code;       // 状态码 (200=成功)
    private String message; // 提示信息
    private T data;         // 响应数据

    public static <T> ResponseResult<T> success(T data) { ... }
    public static <T> ResponseResult<T> success(String message, T data) { ... }
    public static <T> ResponseResult<T> fail(String message) { ... }
    public static <T> ResponseResult<T> fail(int code, String message) { ... }
    public static <T> ResponseResult<T> fail(ResultEnum resultEnum) { ... }
}
```

**响应示例**：
```json
// 成功
{"code": 200, "message": "操作成功", "data": {...}}

// 失败
{"code": 500, "message": "服务器内部错误", "data": null}

// 未认证
{"code": 401, "message": "Token无效或已过期", "data": null}
```

### 14.2 GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseException.class)
    public ResponseResult<?> handleBaseException(BaseException e) { ... }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<?> handleValidException(MethodArgumentNotValidException e) { ... }
    
    @ExceptionHandler(Exception.class)
    public ResponseResult<?> handleException(Exception e) { ... }
}
```

### 14.3 异常枚举

```java
public enum ResultEnum {
    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器内部错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在");
}
```

### 14.4 应用场景分析

| 设计 | 优势 |
|------|------|
| **泛型 ResponseResult\<T\>** | 类型安全，前端可精确推断 data 类型 |
| **静态工厂方法** | `ResponseResult.success(data)` 简洁易用 |
| **BaseException** | 业务异常统一处理，支持自定义 code/message |
| **@RestControllerAdvice** | 全局异常拦截，Controller 无需 try-catch |
| **@Valid 校验** | 配合 `@NotNull/@NotBlank` 等注解自动校验 |

---

## 15. 权限管理 RBAC 模型

### 15.1 数据模型

```
User ──(M:N)── UserRole ──(M:N)── Role ──(M:N)── RolePermission ──(M:N)── Permission
                                                                              │
                                                                     ┌────────┴────────┐
                                                                     │  permissionType  │
                                                                     │  1=MENU  菜单     │
                                                                     │  2=BUTTON 按钮    │
                                                                     │  3=API    接口    │
                                                                     └─────────────────┘
Permission 支持树形结构（parent_id 自引用）:
  系统管理 (MENU)
  ├── 用户管理 (MENU)
  │   ├── 新增用户 (BUTTON)
  │   ├── 编辑用户 (BUTTON)
  │   └── 删除用户 (BUTTON)
  ├── 角色管理 (MENU)
  └── GET /api/user/list (API)
```

### 15.2 权限验证流程

```
用户登录 → JWT Claims 包含 permissions 列表
    ↓
Gateway 将 permissions 写入 X-User-Roles 请求头
    ↓
下游 Controller 使用 @PreAuthorize 注解:
    @PreAuthorize("hasAuthority('user:add')")
    @PostMapping("/user")
```

### 15.3 应用场景分析

| 设计 | 说明 |
|------|------|
| **三级权限** | MENU 控制菜单可见性，BUTTON 控制页面按钮，API 控制接口访问 |
| **树形权限** | `parent_id` 自引用，支持无限级嵌套 |
| **JWT 承载权限** | 权限列表在 JWT Claims 中，网关注入请求头，避免每次都查数据库 |
| **Spring Security 集成** | `@EnableMethodSecurity` + `@PreAuthorize` 注解式鉴权 |

---

## 16. AI 聊天服务集成

### 16.1 技术方案

- **AI 模型**: 通义千问 `qwen-turbo`（阿里云 DashScope API）
- **API 地址**: `https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation`
- **调用方式**: HTTP POST（非流式）

### 16.2 配置

```yaml
chat:
  api-key: ${CHAT_API_KEY:sk-xxx}   # 通过环境变量注入
  api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
  model: qwen-turbo
```

### 16.3 API 接口

```
POST /client/chat/send
Content-Type: application/json

{
  "problemId": 1,              // 可选，关联题目
  "message": "这道题怎么做？",
  "history": [                 // 多轮对话历史
    {"role": "user", "content": "你好"},
    {"role": "assistant", "content": "你好！有什么可以帮助你的？"}
  ]
}
```

### 16.4 应用场景分析

| 场景 | 说明 |
|------|------|
| **代码问题辅助** | 关联题目 ID，AI 可结合题目信息给出针对性建议 |
| **多轮对话** | 通过 `history` 数组传递上下文，支持连续追问 |
| **API Key 隔离** | 通过环境变量注入，不硬编码在配置文件中 |
| **Chat Service 独立** | 无数据库依赖，轻量部署，方便切换 AI 模型 |

---

## 17. 应用场景总结

### 17.1 技术选型理由汇总

| 技术 | 选型理由 |
|------|----------|
| **Spring Boot 3.5.5** | 最新稳定版，支持 Java 21 虚拟线程、GraalVM 原生编译 |
| **Spring Cloud 2025.0.0** | 最新微服务套件，Gateway + LoadBalancer 开箱即用 |
| **Nacos 替代 Eureka** | Eureka 2.0 已停止维护，Nacos 同时提供配置中心能力 |
| **MyBatis-Plus 替代 JPA** | 国内使用广泛，Lambda 查询、分页插件、逻辑删除等开箱即用 |
| **JJWT 0.12** | 支持 Java 21，API 现代化 (`verifyWith().build().parseSignedClaims()`) |
| **Go-Judge 而非 Java 沙箱** | Go 对 Linux cgroup 的支持更原生，资源限制更精确 |
| **Gateway 层 JWT 解析** | 减少一次 Feign 网络调用（~50ms），降低 Auth 服务压力 |
| **Redis 白名单** | 优雅解决 JWT "签发后无法主动失效" 的固有问题 |
| **Docker Compose** | 一键启动 10 个容器，本地开发/测试环境即生产环境 |

### 17.2 架构优势

| 优势 | 体现 |
|------|------|
| **高内聚低耦合** | 5 个独立微服务 + 4 个独立数据库，服务间通过 Feign 解耦 |
| **安全可靠** | JWT + Redis 白名单 + BCrypt 密码 + Gateway 集中鉴权 |
| **可扩展** | 水平扩展只需 `docker-compose scale judge-service=3` |
| **可观测** | SpringDoc 分组文档 + DEBUG 日志 + Nacos 健康检查 |
| **容器化** | 10 个容器统一编排，`docker-compose up -d` 一键部署 |
| **异步判题** | @Async 不阻塞请求，Go-Judge 物理隔离安全执行 |

### 17.3 可改进方向

| 方向 | 建议 |
|------|------|
| **消息队列** | 可用 RabbitMQ/Kafka 替代本地消息表轮询，提升可靠性 |
| **链路追踪** | 集成 SkyWalking 或 Zipkin，可视化微服务调用链 |
| **限流熔断** | 集成 Sentinel，防止判题服务过载 |
| **配置中心** | 将 application.yaml 迁移到 Nacos Config，支持动态刷新 |
| **CI/CD** | 添加 GitHub Actions / Jenkins，自动构建 Docker 镜像并推送 |
| **监控告警** | 集成 Prometheus + Grafana，监控 JVM/接口/QPS |

---

> **文档版本**: v1.0 | **分析引擎**: DeepSeek V4 Pro | **项目**: [EmiyaOJ-Cloud](https://github.com)
