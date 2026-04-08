# 角色定义
你是一名资深的 Java 后端架构师与代码生成专家，请严格遵循以下规范，为我生成一个**可直接运行的** Spring Cloud Alibaba 微服务项目代码骨架。

# 输入材料
我已附上 EmiyaOJ(文件夹名为EmiyaOJ-Old) 的完整单体项目以及其数据库emiya-oj_final.sql。你需要基于这些现有业务逻辑进行代码迁移和重构。

# 核心任务
将单体应用 EmiyaOJ 拆分为微服务架构，并**直接实现符合规范的文件目录结构和核心代码文件内容**。
原项目的代码与数据库表设计是有问题的,尤其是对于Json的处理以及对GoJudge服务的调用,请在迁移过程中进行必要的修正和优化。
以下要求供参考,但请务必按照上述核心任务进行代码实现：
# 严格技术约束（必须遵守）
1. **基础框架**：Spring Boot 3.5.x + Spring Cloud Alibaba 2023.x + Java 21。
2. **组件要求**：
    - 注册中心与配置中心：Nacos（请实现对应的 `bootstrap.yaml` 配置,并在每个微服务中给出配置）。
    - 远程调用：OpenFeign + LoadBalancer。
    - API 网关：Spring Cloud Gateway。
    - 鉴权：JWT（解析在网关层，验权在服务层）。
    - 数据库：MySQL 8.0 + MyBatis-Plus 3.5.x（各服务独立数据库 Schema）。
3. **模块结构强制规范**：
   每个独立的微服务必须采用 **父子多模块** 结构。以认证服务为例，必须包含以下三个子模块，且需明确 Maven `pom.xml` 的依赖关系：
    - `auth-dto`：存放对外暴露的 DTO 和 VO（不依赖任何业务模块）。
    - `auth-api`：存放 `@FeignClient` 接口定义，**仅依赖** `auth-dto`。
    - `auth-service`：核心业务实现、Controller、Mapper，依赖 `auth-api` 和 `auth-dto`。

# 实现格式要求（重要）
请分章节实现以下内容，**每一章节必须包含具体的代码块**，严禁只实现文字描述。

## 第一部分：项目父级 POM 与公共模块
1. 请实现根目录 `pom.xml` 的完整内容，重点包含 `dependencyManagement` 对 Spring Cloud Alibaba 和公共模块的版本管理。
2. 实现 `common-core` 模块（原 `oj-common`）的目录结构和以下核心文件代码：
    - `JwtUtil.java`（JWT 生成与解析）
    - `RedisUtil.java`
    - `GlobalExceptionHandler.java`（统一异常处理，返回标准 JSON 格式）
    - `ResponseResult.java`（统一响应体）

## 第二部分：认证服务 (EmiyaOJ-Auth) 代码生成
请严格按照三层模块规范生成代码。需要实现以下文件的**完整代码**：

**1. 目录结构**
请实现 `EmiyaOJ-Auth` 文件夹下的完整树形结构，包含三个子模块及 Java 包路径。

**2. auth-dto 模块**
- 实现 `UserLoginDTO.java`、`UserLoginVO.java` 和 `UserAuthDTO.java`（包含 userId, username, permissions 列表）。

**3. auth-api 模块**
- 实现 `AuthFeignClient.java` 接口，定义 `GET /auth/user/parse-token` 方法，用于其他服务解析 Token。

**4. auth-service 模块**
- 实现 `application.yaml` 配置（Nacos 连接、MySQL 数据源、Redis 配置）。
- 实现 `AuthController.java`，实现 `/auth/login` 和 `/auth/logout` 接口，逻辑需从原项目的 `AuthController` 中迁移并适配微服务环境。
- 实现 `SecurityConfig.java` 和 `JwtTokenOncePerRequestFilter.java` 的微服务适配版本（注意：此时 Filter 仅处理服务内部权限校验，网关已做 Token 解析）。

## 第三部分：网关服务 (EmiyaOJ-Gateway) 代码生成
1. 实现网关模块的 `application.yaml` 完整配置，需包含：
    - Nacos 服务发现配置。
    - 针对 `/auth/**`、`/problem/**`、`/submission/**` 的路由断言规则。
2. 实现网关层的核心过滤器 `AuthGlobalFilter.java`，实现以下逻辑：
    - 从请求头 `Authorization` 中提取 Bearer Token。
    - 调用 **Auth 服务** 的 Feign 接口解析 Token 获取用户信息。
    - 将 `X-User-Id` 和 `X-User-Roles` 写入请求头转发给下游服务。
3. 实现 Feign 调用必要的配置类 `FeignConfig.java` 处理 Token 透传。

## 第四部分：题目服务与判题服务拆分示例
1. **Problem Service**：
    - 实现 `EmiyaOJ-Problem` 的目录结构（problem-dto, problem-api, problem-service）。
    - 实现 `ProblemController.java` 的微服务版本代码，展示如何利用 `@RequestHeader("X-User-Id")` 获取操作用户 ID。

2. **Judge Service**：
    - 实现 `EmiyaOJ-Judge` 的目录结构。
    - 实现 `JudgeController.java` 中接收判题请求的接口 `/judge/submit` 代码。
    - 实现 Feign 客户端 `SubmissionFeignClient` 的定义（用于判题完成后回调更新提交记录状态）。

## 第五部分：分布式事务处理策略代码
请针对 **"提交记录创建 -> 判题完成更新"** 这一跨服务流程，实现基于 **本地消息表** 或 **Seata AT 模式** 的具体实现代码示例。
- 如果使用 Seata，请实现 `application.yaml` 中的 Seata 配置。
- 如果使用本地消息表，请在 `submission-service` 中实现 `SubmissionServiceImpl.java` 的 `createSubmission` 方法逻辑（包含写入本地消息表的 SQL 和定时任务扫描代码）。

## 第六部分：Docker Compose 开发环境编排
请实现一份 `docker-compose.yml` 文件，包含以下服务的容器定义：
- Nacos 3.2.x (单机模式)
- MySQL 8.0
- Redis 7.x

# 附加指令
- 所有代码注释请使用中文。
