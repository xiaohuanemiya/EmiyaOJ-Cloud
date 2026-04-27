# EmiyaOJ-Cloud 技术架构与应用场景说明

> 文档目标：系统化说明本项目使用了哪些技术、分别解决什么问题、在代码中的落点在哪、对应哪些业务场景。
>
> 适用读者：项目成员、答辩评审、面试官、二次开发者。

---

## 1. 项目总体定位

`EmiyaOJ-Cloud` 是一个基于 **Spring Cloud Alibaba** 的在线判题微服务系统，核心业务覆盖：

- 用户认证与权限管理（Auth）
- 题目、测试用例、语言配置管理（Problem）
- 提交与异步判题（Judge + Go-Judge）
- 博客社区（Blog）
- AI 编程助手（Chat）
- 统一接入与鉴权转发（Gateway）

系统采用“网关统一入口 + 业务服务解耦 + Nacos 服务治理 + Redis 状态加速 + MySQL 持久化”的典型云原生后端架构。

---

## 2. 微服务拆分与模块结构

## 2.1 顶层模块

根工程（`pom.xml`）定义了以下子模块：

- `EmiyaOJ-Common`：公共能力（响应体、异常、JWT工具、Feign透传等）
- `EmiyaOJ-Gateway`：网关服务
- `EmiyaOJ-Auth`：认证域（`auth-dto / auth-api / auth-service`）
- `EmiyaOJ-Problem`：题目域（`problem-dto / problem-api / problem-service`）
- `EmiyaOJ-Judge`：判题域（`judge-dto / judge-api / judge-service`）
- `EmiyaOJ-Blog`：博客域（`blog-dto / blog-api / blog-service`）
- `EmiyaOJ-Chat`：AI 聊天域（`chat-dto / chat-api / chat-service`）

## 2.2 设计价值

这种拆分方式带来以下收益：

1. **边界清晰**：每个业务域独立部署、独立演进。
2. **依赖可控**：通过 `*-api` 和 `*-dto` 层暴露稳定契约。
3. **复用统一**：公共工具和拦截逻辑下沉到 `EmiyaOJ-Common`。
4. **便于扩展**：后续新增如通知服务、榜单服务时，几乎可平移当前模式。

---

## 3. 技术栈全景

## 3.1 基础框架

- **Java 21**
- **Spring Boot 3.5.x**
- **Spring Cloud 2025.x**
- **Spring Cloud Alibaba 2025.x**
- **Maven 多模块构建**

## 3.2 核心中间件

- **Nacos**：服务注册发现 + 配置中心
- **MySQL 8.0**：业务持久化
- **Redis 7**：Token 白名单、缓存加速
- **Gateway**：统一入口、路由、网关层鉴权
- **Go-Judge**：代码编译/运行沙箱

## 3.3 业务开发技术

- **MyBatis-Plus**：ORM、分页、逻辑删除
- **OpenFeign + LoadBalancer**：服务间调用
- **Spring Security**：服务内权限控制（Auth）
- **JJWT**：JWT 生成与解析
- **SpringDoc OpenAPI**：接口文档
- **WebClient / RestTemplate**：外部 HTTP 调用
- **@Async**：异步判题

---

## 4. 关键技术与应用场景（重点）

> 本节重点回答：用了什么技术、为什么用、在哪用、解决了什么业务问题。

## 4.1 微服务架构（Microservices）

**技术落点**

- 根模块聚合管理（`pom.xml`）
- 各服务独立启动类：
  - `AuthApplication`
  - `ProblemApplication`
  - `JudgeApplication`
  - `BlogApplication`
  - `ChatApplication`
  - `GatewayApplication`

**应用场景**

- 用户登录高峰不影响判题任务执行。
- 判题服务可独立扩容，避免博客/聊天业务挤占判题资源。
- AI 聊天服务（外部 API 依赖强）可单独治理超时/限流策略。

**业务价值**

- 提升系统弹性与可维护性。
- 支持按域分工开发、并行迭代。

---

## 4.2 Nacos：服务注册与配置中心

**技术落点**

- 各服务 `bootstrap.yaml`：
  - `spring.cloud.nacos.discovery`
  - `spring.cloud.nacos.config`
- 启动类普遍开启 `@EnableDiscoveryClient`

**应用场景**

1. **服务发现**：Gateway 按服务名路由到 `auth-service/problem-service/judge-service...`
2. **环境配置统一**：开发/测试/生产环境可切换不同配置集或命名空间。
3. **动态治理基础**：服务地址变化不需要硬编码。

**业务价值**

- 避免“IP+端口硬编码”导致的运维脆弱性。
- 支撑微服务弹性伸缩与灰度基础能力。

---

## 4.3 Gateway：统一入口与网关鉴权

**技术落点**

- `EmiyaOJ-Gateway/src/main/resources/application.yaml`（路由规则）
- `AuthGlobalFilter`（全局认证过滤器）
- `GatewayWhitelistProperties`（白名单）

**应用场景**

1. 对外单入口（`8080`），统一路由到各微服务。
2. 在网关层完成 JWT 校验，减少下游重复校验压力。
3. 将身份上下文透传给下游：
   - `X-User-Id`
   - `X-User-Name`
   - `X-User-Roles`

**业务价值**

- 安全策略收敛到入口层。
- 下游服务专注业务逻辑，无需重复解析 Token。

---

## 4.4 JWT 登录校验（你提到的重点）

**技术落点**

- `AuthService`：登录成功后生成 JWT
- `JwtUtil`：统一 JWT 创建/解析
- `AuthGlobalFilter`：网关层解析 JWT
- `JwtTokenOncePerRequestFilter`：Auth 服务内补充校验

**应用场景**

### 场景 A：用户登录

1. 用户调用 `/auth/login`
2. `AuthService` 认证成功后生成 JWT（含 `userId/username/permissions`）
3. 返回 token 给前端

### 场景 B：访问业务接口

1. 前端携带 `Authorization: Bearer <token>`
2. 网关 `AuthGlobalFilter` 验签并校验 Redis 白名单
3. 网关注入用户上下文头后转发
4. 下游服务读取 `X-User-Id` 执行业务

### 场景 C：用户登出

1. 调用 `/auth/logout`
2. 删除 Redis 中 `token_{userId}`
3. Token 失效（即使未到 JWT 自然过期时间）

**业务价值**

- 兼顾“无状态 JWT”与“可主动失效”的需求。
- 支持网关层统一认证，提高系统一致性。

---

## 4.5 Redis：Token 白名单与会话增强

**技术落点**

- `AuthService` 写入/删除 `token_{userId}`
- `AuthGlobalFilter` 与 `JwtTokenOncePerRequestFilter` 校验 key 是否存在，并刷新 TTL

**应用场景**

1. 登录态校验
2. 登出立即失效
3. 滑动续期（活跃用户自动延长有效期）

**业务价值**

- 补齐 JWT 天生“难撤销”的痛点。
- 登录态可控，安全性更高。

---

## 4.6 OpenFeign + LoadBalancer：服务间调用

**技术落点**

- `ProblemFeignClient`（Judge -> Problem）
- `SubmissionFeignClient`、`AuthFeignClient` 等
- `JudgeApplication` 开启 `@EnableFeignClients`
- `FeignConfig`（透传用户头信息）

**应用场景**

1. 判题前，Judge 服务查询 Problem 服务：
   - 题目详情
   - 测试用例
   - 语言配置
2. 通过服务名调用而非固定地址，配合 Nacos 动态发现实例。

**业务价值**

- 跨服务调用编程模型统一、开发效率高。
- 支持实例级负载均衡，增强稳定性。

---

## 4.7 MyBatis-Plus + MySQL：核心业务持久化

**技术落点**

- 各服务 `application.yaml` 中 datasource 配置
- mapper + service 组合（如 `SubmissionMapper`, `ProblemService`）
- 逻辑删除字段（`deleted`）与分页查询

**应用场景**

- 用户/角色/权限管理
- 题目、标签、测试用例管理
- 提交记录与判题状态追踪
- 博客、评论、收藏管理

**业务价值**

- 兼顾开发效率与 SQL 可控性。
- 逻辑删除避免数据误删带来的不可逆风险。

---

## 4.8 异步判题（@Async）

**技术落点**

- `JudgeApplication` 开启 `@EnableAsync`
- `JudgeExecutor#executeJudgeAsync()` 使用 `@Async`
- `SubmissionService` 同步创建记录后触发异步执行

**应用场景**

1. 用户提交代码后接口立即返回，不阻塞前端。
2. 后台异步执行编译和多测试用例运行。
3. 结果写回数据库，前端通过提交记录接口轮询。

**业务价值**

- 显著优化用户体验（低响应延迟）。
- 避免长耗时判题占用 Web 线程。

---

## 4.9 Go-Judge 沙箱执行

**技术落点**

- `GoJudgeService`（HTTP 调用 `/run`、`/file/{id}`）
- `JudgeExecutor` 负责编译/运行流程编排
- `docker-compose.yml` 中 `go-judge` 容器

**应用场景**

- C/C++/Java/Go/Python 多语言编译与执行
- 逐测试用例运行，统计：
  - 时间
  - 内存
  - 通过率
- 根据运行状态映射 OJ 结果：AC/WA/TLE/MLE/RE/CE/SE/PA 等

**业务价值**

- 判题执行环境与业务服务解耦，安全隔离更好。
- 易于替换/升级沙箱实现。

---

## 4.10 Spring Security（服务内权限控制）

**技术落点**

- `SecurityConfig`
- `JwtTokenOncePerRequestFilter`

**应用场景**

- Auth 服务内部接口保护（例如 `/auth/logout`）。
- 白名单放行登录与文档接口。
- 统一 401/403 JSON 返回。

**业务价值**

- 在网关鉴权之外，服务层仍有安全兜底。

---

## 4.11 OpenAPI/Swagger 文档

**技术落点**

- 各服务 `springdoc` 配置
- Controller 层 `@Tag`、`@Operation`

**应用场景**

- 后前端协作联调
- 接口验收与测试
- 快速定位接口契约变更

**业务价值**

- 降低沟通成本，提升开发协同效率。

---

## 4.12 Docker Compose 本地一体化环境

**技术落点**

- 根目录 `docker-compose.yml`

**编排内容**

- 基础设施：MySQL、Redis、Nacos
- 业务服务：Gateway、Auth、Problem、Judge、Blog、Chat
- 判题沙箱：Go-Judge

**应用场景**

- 本地快速拉起完整链路
- 演示环境统一
- 新成员零散依赖最小化

---

## 5. 关键业务链路与技术协同

## 5.1 链路一：登录鉴权链路

1. 用户调用 `auth-service` 登录
2. `AuthService` 使用 Spring Security 完成认证
3. 通过 `JwtUtil` 生成 Token
4. Redis 写入 `token_{userId}`
5. 后续业务请求经 Gateway 校验 JWT + Redis 白名单
6. Gateway 透传身份头部至下游服务

**体现技术协同**：Spring Security + JWT + Redis + Gateway

## 5.2 链路二：提交判题链路

1. 用户请求 `/judge/submit`
2. Gateway 透传用户身份
3. Judge 服务通过 Feign 调 Problem 服务获取题目/语言/用例
4. Submission 记录先落库（Pending）
5. 异步线程调用 Go-Judge 编译/执行
6. 汇总结果并更新 Submission 状态

**体现技术协同**：Gateway + Feign + MyBatis + Async + Go-Judge

## 5.3 链路三：AI 助手链路

1. 用户调用 `/client/chat/send`
2. Chat 服务封装历史消息与系统提示词
3. 调用 DashScope（Qwen）模型接口
4. 将模型回复封装后返回

**体现技术协同**：REST 调用 + 模型提示词工程 + 微服务封装

---

## 6. 数据与领域模型概览

核心表按业务域分组如下：

- **Auth 域**：`user`, `role`, `permission`, `user_role`, `role_permission`
- **Problem 域**：`problem`, `tag`, `problem_tag`, `test_case`, `language`
- **Judge 域**：`submission`, `submission_result`
- **Blog 域**：`blog`, `blog_comment`, `blog_star`, `blog_tag`, `blog_tag_association`, `user_blog`
- **平台辅助**：`operation_log`

设计特点：

- 多数业务表具备时间字段，便于审计与追踪。
- 通过中间表实现多对多关系（用户-角色、角色-权限、题目-标签）。
- Blog 模块使用触发器维护 `user_blog` 聚合统计。

---

## 7. 安全与稳定性设计

## 7.1 安全策略

1. **入口鉴权**：网关优先校验 JWT。
2. **会话可撤销**：Redis 白名单机制支持登出立即失效。
3. **服务兜底**：Auth 服务自身仍保留 Security Filter 校验。
4. **权限透传**：`X-User-Roles` 可用于下游细粒度授权扩展。

## 7.2 稳定性策略

1. **服务注册发现**：通过 Nacos 避免硬编码地址。
2. **异步解耦**：判题重任务异步执行。
3. **状态可观测**：Submission 全生命周期状态字段可追踪。
4. **容器化部署**：统一环境降低“本地可跑/线上失败”概率。

---

## 8. 项目当前架构优势与可优化方向

## 8.1 已具备优势

- 微服务边界清晰，模块职责明确
- 网关层统一鉴权，减少重复逻辑
- 判题链路异步化，用户体验较好
- DTO/API 分层规范，跨服务契约明确
- Docker Compose 提供一键化环境

## 8.2 可继续增强（建议）

1. **链路追踪**：引入 SkyWalking / OpenTelemetry。
2. **熔断限流**：在 Gateway 与 Feign 层补充 Resilience4j。
3. **任务队列化**：判题异步可升级为 MQ（RabbitMQ/Kafka）解耦。
4. **统一配置治理**：Nacos 配置分组进一步标准化（dev/test/prod）。
5. **审计与告警**：对登录失败、判题异常增加指标与告警。

---

## 9. 结论（面向答辩/汇报）

`EmiyaOJ-Cloud` 的技术路线可以概括为：

- 用 **微服务 + Nacos** 实现可扩展的服务治理基础；
- 用 **Gateway + JWT + Redis** 构建统一认证鉴权链路；
- 用 **Feign + MyBatis-Plus** 实现高效的服务协作与数据持久化；
- 用 **@Async + Go-Judge** 支撑判题场景中的高耗时任务；
- 用 **Docker Compose** 实现研发环境的一致化与可复现性。

这套技术组合与 OJ 系统的业务形态高度匹配，既满足当前功能落地，也为后续扩展（竞赛、排行榜、消息通知、分布式任务）预留了足够空间。
