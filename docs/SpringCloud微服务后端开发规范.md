# SpringCloud 微服务后端开发规范

> **版本**: V1.0 | **日期**: 2026-06-03 | **JDK**: 21 | **Spring Boot**: 3.5.5 | **Spring Cloud**: 2025.0.0
>
> 基于 EmiyaOJ-Cloud 项目实践提炼，适用于 Spring Cloud Alibaba 微服务架构。

---

## 目录

1. [项目结构规范](#1-项目结构规范)
2. [命名规范](#2-命名规范)
3. [分层架构规范](#3-分层架构规范)
4. [Controller 层规范](#4-controller-层规范)
5. [Service 层规范](#5-service-层规范)
6. [Mapper 层规范（MyBatis-Plus）](#6-mapper-层规范mybatis-plus)
7. [DTO/VO/Entity 规范](#7-dtovovoentity-规范)
8. [Feign 远程调用规范](#8-feign-远程调用规范)
9. [异常处理规范](#9-异常处理规范)
10. [API 响应规范](#10-api-响应规范)
11. [配置管理规范](#11-配置管理规范)
12. [安全规范](#12-安全规范)
13. [数据库规范](#13-数据库规范)
14. [Git 提交规范](#14-git-提交规范)
15. [代码质量规范](#15-代码质量规范)

---

## 1. 项目结构规范

### 1.1 Maven 多模块标准结构

```
EmiyaOJ-{Service}/                      # 微服务父模块 (pom)
├── pom.xml                             # 聚合 POM
│
├── {service}-api/                      # Feign 接口定义 + 降级实现
│   └── src/main/java/com/emiyaoj/{service}/feign/
│       ├── XxxFeignClient.java         # Feign 客户端接口
│       └── fallback/                   # 降级工厂类
│           └── XxxFeignClientFallback.java
│
├── {service}-dto/                      # 跨服务共享的数据传输对象
│   └── src/main/java/com/emiyaoj/{service}/
│       ├── dto/                        # 请求 DTO（入参）
│       │   ├── XxxSaveDTO.java
│       │   └── XxxQueryDTO.java
│       └── vo/                         # 响应 VO（出参）
│           └── XxxVO.java
│
└── {service}-service/                  # 业务实现（独立启动）
    ├── Dockerfile
    └── src/main/java/com/emiyaoj/{service}/
        ├── {Service}Application.java   # 启动类
        ├── config/                     # 本服务私有配置
        │   ├── AsyncConfig.java
        │   └── XxxProperties.java
        ├── controller/                 # REST 控制器
        │   └── XxxController.java
        ├── service/                    # 业务服务接口
        │   ├── IXxxService.java
        │   └── impl/
        │       └── XxxServiceImpl.java
        ├── domain/                     # 领域对象
        │   ├── entity/                 # 数据库实体
        │   │   └── Xxx.java
        │   ├── pojo/                   # 值对象/嵌入式对象
        │   └── enums/                  # 枚举类
        │       └── XxxStatus.java
        └── mapper/                     # MyBatis Mapper 接口
            └── XxxMapper.java
```

### 1.2 三模块分离原则

| 模块 | 打包方式 | 依赖方 | 内容 |
|------|---------|--------|------|
| `{service}-api` | jar | 其他微服务 | Feign 接口 + 降级实现（仅依赖 dto） |
| `{service}-dto` | jar | 所有模块 | DTO、VO、Query 对象（无业务逻辑） |
| `{service}-service` | jar（Spring Boot） | 无 | Controller、Service、Mapper、Domain、配置 |

> **关键规则**: `api` 和 `dto` 模块是轻量级 JAR，**禁止**引入 Spring Boot 全套依赖。只有 `service` 模块可以有独立启动能力。

### 1.3 公共模块 EmiyaOJ-Common 职责

```
com.emiyaoj.common/
├── config/              # 可复用的 @Configuration（MyBatis-Plus、Jackson、Redis、Feign）
├── constant/            # 跨服务常量、枚举
├── domain/              # 通用领域对象（ResponseResult、PageDTO、PageVO）
├── exception/           # 全局异常基类
├── handler/             # GlobalExceptionHandler
├── interceptor/         # 通用拦截器
├── properties/          # 可复用的 @ConfigurationProperties
└── utils/               # 工具类（JWT、Redis、BaseContext）
```

> **规则**: Common 模块**不依赖**任何业务模块（api/dto），只包含纯基础设施代码。

---

## 2. 命名规范

### 2.1 包命名

```
com.emiyaoj.{服务}.{层次}

示例:
com.emiyaoj.auth.controller
com.emiyaoj.problem.service.impl
com.emiyaoj.judge.domain.entity
com.emiyaoj.blog.mapper
```

> 基础包名统一为 `com.emiyaoj`，不包含 `cloud` 等冗余后缀。

### 2.2 类命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | 名词 + `Controller` | `AuthController`, `ProblemController`, `ContestController` |
| Service 接口 | `I` + 名词 + `Service` | `IUserService`, `IBlogService`, `IChatService` |
| Service 实现 | 名词 + `ServiceImpl` | `UserServiceImpl`, `BlogServiceImpl` |
| Mapper | 名词 + `Mapper` | `UserMapper`, `BlogMapper` |
| Entity | 名词（与表名对应） | `User`, `Problem`, `Submission` |
| DTO（入参） | 名词 + `DTO` | `UserLoginDTO`, `BlogSaveDTO`, `ProblemQueryDTO` |
| VO（出参） | 名词 + `VO` | `UserVO`, `BlogVO`, `ProblemVO` |
| Feign Client | 名词 + `FeignClient` | `AuthFeignClient`, `ProblemFeignClient` |
| 工具类 | 名词 + `Util` | `JwtUtil`, `RedisUtil` |
| 配置类 | 名词 + `Config` / `Properties` | `MybatisPlusConfig`, `JwtProperties` |
| 异常类 | 名词 + `Exception` | `BaseException`, `BadRequestException` |

### 2.3 方法命名

| 操作 | Controller 方法 | Service 方法 |
|------|----------------|-------------|
| 分页查询 | `page()` / `list()` | `selectXxxPage()` / `queryXxxPage()` |
| 单个查询 | `getById()` | `selectXxxById()` / `getXxxById()` |
| 列表查询 | `list()` / `batch()` | `selectAllXxx()` / `selectXxxList()` |
| 新增 | `save()` | `saveXxx()` / `insertXxx()` |
| 修改 | `update()` | `updateXxx()` |
| 删除 | `delete()` / `deleteBatch()` | `deleteXxx()` / `deleteXxxList()` |
| 状态变更 | `updateStatus()` | `updateXxxStatus()` |
| 关联操作 | `assignRoles()` | `assignRolesToUser()` |

### 2.4 变量命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 实体变量 | camelCase | `User user`, `Problem problem` |
| 集合变量 | 名词复数 | `List<User> users`, `Set<Long> roleIds` |
| Mapper 注入 | `{实体小写}Mapper` | `userMapper`, `blogMapper` |
| Service 注入 | `{实体小写}Service` | `userService`, `blogService` |
| Feign 注入 | `{实体小写}FeignClient` | `problemFeignClient` |
| DTO/VO 构造 | `dto` / `vo` / `saveDTO` | `blogSaveDTO`, `userVO` |

---

## 3. 分层架构规范

### 3.1 标准分层职责

```
┌─────────────────────────────────────┐
│  Controller 层                      │
│  - @RestController + @RequestMapping│
│  - 接收请求、参数校验(@Valid)        │
│  - 调用 Service，返回 ResponseResult│
│  - 禁止包含业务逻辑                  │
├─────────────────────────────────────┤
│  Service 层                         │
│  - @Service + @Transactional       │
│  - 业务逻辑、数据转换（Entity↔VO）  │
│  - 跨服务调用（Feign）编排          │
│  - 调用 Mapper 层                   │
├─────────────────────────────────────┤
│  Mapper 层                          │
│  - extends BaseMapper<T>           │
│  - 数据访问，与数据库一一对应        │
│  - 禁止包含业务逻辑                  │
├─────────────────────────────────────┤
│  Domain 层                          │
│  - Entity（数据库实体）             │
│  - Enum（状态枚举）                 │
│  - 纯数据对象（无行为）             │
└─────────────────────────────────────┘
```

### 3.2 依赖方向（单向依赖）

```
Controller → Service 接口 → Service 实现 → Mapper → Database
                   ↓
              DTO / VO / Entity  （纯数据，无依赖）
```

> **规则**: Controller **绝不**直接调用 Mapper。Service 实现通过接口暴露，Controller 依赖接口而非实现。

### 3.3 跨层禁止事项

| 禁止 | 说明 |
|------|------|
| Controller 直接调用 Mapper | 必须经过 Service 层 |
| Controller 包含业务判断 | 多条件判断放到 Service |
| Service 返回 Entity | 必须转换为 VO/DTO 再返回 |
| Mapper 包含业务逻辑 | Mapper 只做数据访问 |
| DTO/VO 包含业务方法 | DTO/VO 是纯数据载体 |

---

## 4. Controller 层规范

### 4.1 标准 Controller 模板

```java
package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ProblemQueryDTO;
import com.emiyaoj.problem.dto.ProblemSaveDTO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 题目管理控制器
 * <p>
 * 微服务版本：通过网关转发的 X-User-Id 请求头获取当前操作用户 ID
 */
@Tag(name = "题目管理")
@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping("/list")
    @Operation(summary = "分页查询题目列表")
    public ResponseResult<PageVO<ProblemVO>> list(ProblemQueryDTO queryDTO) {
        PageVO<ProblemVO> pageVO = problemService.queryProblemPage(queryDTO);
        return ResponseResult.success(pageVO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询题目详情")
    public ResponseResult<ProblemVO> getById(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        ProblemVO vo = problemService.getProblemDetail(id);
        if (vo == null) {
            return ResponseResult.fail(404, "题目不存在");
        }
        return ResponseResult.success(vo);
    }

    @PostMapping
    @Operation(summary = "新增题目")
    public ResponseResult<Boolean> save(
            @Valid @RequestBody ProblemSaveDTO dto,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("用户 {} 创建题目 {}", userId, dto.getTitle());
        boolean success = problemService.saveProblem(dto, userId);
        return ResponseResult.success(success);
    }

    @PutMapping
    @Operation(summary = "更新题目")
    public ResponseResult<Boolean> update(
            @Valid @RequestBody ProblemSaveDTO dto,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = problemService.updateProblem(dto, userId);
        return ResponseResult.success(success);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public ResponseResult<Boolean> delete(
            @Parameter(description = "题目ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = problemService.deleteProblem(id, userId);
        return ResponseResult.success(success);
    }
}
```

### 4.2 注解使用规范

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Tag(name = "...")` | 类 | Swagger 分组标签，中文/英文均可 |
| `@Operation(summary = "...")` | 方法 | API 功能摘要 |
| `@Parameter(description = "...")` | 参数 | 参数说明 |
| `@Parameter(hidden = true)` | `X-User-Id` 等内部参数 | 在 Swagger 中隐藏 |
| `@Valid` | 入参 DTO | 触发 JSR-303 校验 |
| `@RequiredArgsConstructor` | 类 | Lombok 构造器注入 |
| `@Slf4j` | 类 | 日志对象 |

### 4.3 请求映射语义

| HTTP 方法 | 注解 | 用途 | 示例 |
|-----------|------|------|------|
| GET | `@GetMapping` | 查询（无请求体） | `GET /problem/list`, `GET /problem/{id}` |
| POST | `@PostMapping` | 新增 / 复杂查询 | `POST /problem`（新增）, `POST /role/page`（分页查询） |
| PUT | `@PutMapping` | 全量更新 | `PUT /problem`（更新题目） |
| DELETE | `@DeleteMapping` | 删除 | `DELETE /problem/{id}` |

### 4.4 用户上下文获取

```java
// 方式一：直接从请求头获取（✅ 推荐）
@PostMapping
public ResponseResult<Boolean> save(
        @RequestBody XxxSaveDTO dto,
        @RequestHeader("X-User-Id") Long userId) {  // 网关注入
    // ...
}

// 方式二：使用 BaseContext（适用于无法从 Controller 传参的场景）
Long userId = BaseContext.getCurrentId();

// 查权限时需要同时获取 roles
@RequestHeader(value = "X-User-Roles", required = false) String permissions
```

### 4.5 Controller 禁止事项

| 禁止 | 正确做法 |
|------|---------|
| `try-catch` 包裹整个方法体 | 统一由 `GlobalExceptionHandler` 处理 |
| 直接返回 Entity | 转换为 VO 后返回 |
| 在 Controller 中拼接 SQL 条件 | 封装到 Service 或 QueryDTO 中 |
| 多个 Service 组合调用写在 Controller | 封装到 Service 中 |

---

## 5. Service 层规范

### 5.1 接口与实现分离

```java
// ✅ 接口（定义在 service 包下）
package com.emiyaoj.problem.service;

public interface IProblemService {
    PageVO<ProblemVO> queryProblemPage(ProblemQueryDTO queryDTO);
    ProblemVO getProblemDetail(Long id);
    boolean saveProblem(ProblemSaveDTO dto, Long operatorId);
    boolean updateProblem(ProblemSaveDTO dto, Long operatorId);
    boolean deleteProblem(Long id, Long operatorId);
}

// ✅ 实现（定义在 service.impl 包下）
package com.emiyaoj.problem.service.impl;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemServiceImpl implements IProblemService {
    // ...
}
```

> **注意**: 当 Service 直接继承 MyBatis-Plus 的 `ServiceImpl<M, T>` 时，可以省略接口，直接用类作为 Bean；但当需要 Feign 暴露或 Mock 测试时，**必须**要有接口。

### 5.2 MyBatis-Plus Service 模式

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService extends ServiceImpl<ProblemMapper, Problem> {

    private final ProblemTagMapper problemTagMapper;
    private final TagMapper tagMapper;

    /**
     * 分页查询题目列表
     */
    public PageVO<ProblemVO> queryProblemPage(ProblemQueryDTO queryDTO) {
        // 1. 构建 MyBatis-Plus 分页对象
        Page<Problem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Problem::getTitle, queryDTO.getTitle())
               .eq(queryDTO.getDifficulty() != null, Problem::getDifficulty, queryDTO.getDifficulty())
               .eq(queryDTO.getStatus() != null, Problem::getStatus, queryDTO.getStatus())
               .orderByDesc(Problem::getCreateTime);

        // 3. 执行分页查询
        this.page(page, wrapper);

        // 4. 转换为 VO 返回
        return PageVO.of(page, this::convertToVO);
    }

    /**
     * Entity → VO 转换
     */
    private ProblemVO convertToVO(Problem problem) {
        ProblemVO vo = new ProblemVO();
        BeanUtils.copyProperties(problem, vo);
        // 加载关联数据（标签等）
        vo.setTags(loadTags(problem.getId()));
        return vo;
    }
}
```

### 5.3 事务管理规范

```java
// ✅ 写操作必须加事务
@Transactional(rollbackFor = Exception.class)
public boolean saveProblem(ProblemSaveDTO dto, Long operatorId) {
    Problem problem = new Problem();
    BeanUtils.copyProperties(dto, problem);
    problem.setAuthorId(operatorId);

    // 保存题目
    this.save(problem);

    // 保存标签关联
    if (!CollectionUtils.isEmpty(dto.getTagIds())) {
        saveProblemTags(problem.getId(), dto.getTagIds());
    }

    return true;
}

// ✅ 读操作不加事务（提升性能）
public ProblemVO getProblemDetail(Long id) {
    Problem problem = this.getById(id);
    if (problem == null) return null;
    return convertToVO(problem);
}
```

### 5.4 事务注意事项

| 规范 | 说明 |
|------|------|
| `rollbackFor = Exception.class` | **必须**指定，Spring 默认只回滚 RuntimeException |
| 避免长事务 | 事务内不要做 RPC 调用、文件 IO 等耗时操作 |
| 避免事务嵌套 | 不同 Service 之间使用 Feign 调用，不做嵌套事务 |
| 读操作不加 `@Transactional` | 避免不必要的数据库连接占用 |

---

## 6. Mapper 层规范（MyBatis-Plus）

### 6.1 Mapper 接口定义

```java
package com.emiyaoj.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.problem.domain.entity.Problem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
    // 简单 CRUD 继承 BaseMapper 即可，无需编写
    // 复杂查询可在此定义方法 + XML 实现
}
```

### 6.2 LambdaQueryWrapper 使用规范

```java
// ✅ 推荐：Lambda 表达式（类型安全）
LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Problem::getStatus, 1)
       .like(StringUtils.hasText(title), Problem::getTitle, title)
       .in(!CollectionUtils.isEmpty(ids), Problem::getId, ids)
       .orderByDesc(Problem::getCreateTime);

// ❌ 不推荐：字符串列名（容易写错，重构困难）
QueryWrapper<Problem> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1);
```

### 6.3 分页查询标准流程

```java
// 1. 创建 MyBatis-Plus 分页对象
Page<Submission> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());

// 2. 构建条件
LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(problemId != null, Submission::getProblemId, problemId)
       .eq(userId != null, Submission::getUserId, userId)
       .orderByDesc(Submission::getCreateTime);

// 3. 执行分页
this.page(page, wrapper);  // 或 submissionMapper.selectPage(page, wrapper)

// 4. 转换为统一 VO
return PageVO.of(page, this::convertToVO);
```

### 6.4 MyBatis-Plus 关键配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true        # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志（仅开发环境）
  global-config:
    db-config:
      id-type: assign_id                      # 雪花算法
      logic-delete-field: deleted             # 逻辑删除字段
      logic-delete-value: 1                   # 已删除值
      logic-not-delete-value: 0               # 未删除值
      update-strategy: not_null               # 仅更新非空字段
```

---

## 7. DTO/VO/Entity 规范

### 7.1 对象分类与职责

| 类型 | 包位置 | 职责 | 可否跨服务 |
|------|--------|------|-----------|
| **Entity** | `domain.entity` | 与数据库表一一映射，包含 ORM 注解 | ❌ 仅本服务内部 |
| **DTO（入参）** | `{service}-dto` 模块 | 接收前端请求参数，含校验注解 | ✅ 可被调用方引用 |
| **VO（出参）** | `{service}-dto` 模块 | 返回给前端的数据视图 | ✅ 可被调用方引用 |
| **QueryDTO** | `{service}-dto` 模块 | 分页查询/条件查询参数封装 | ✅ |

### 7.2 Entity 规范

```java
package com.emiyaoj.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blog")
public class Blog {

    @TableId(type = IdType.ASSIGN_ID)     // 雪花算法生成
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private Integer blogType;              // 0-普通, 1-题解
    private Long problemId;                // 关联题目（可为 null）
    private Integer viewCount;
    private Integer likeCount;
    private Integer auditStatus;           // 审核状态

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 7.3 DTO（入参）规范

```java
package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BlogSaveDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private List<Long> tagIds;             // 关联标签（可选）
}
```

### 7.4 QueryDTO 规范

```java
package com.emiyaoj.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.emiyaoj.common.domain.PageDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlogQueryDTO extends PageDTO {  // 继承通用分页参数

    @Size(max = 50, message = "标题搜索最多50字")
    private String title;

    private Integer blogType;              // 0-普通, 1-题解
    private Long tagId;
    private String sortBy;                 // createTime / viewCount / likeCount
    private Integer auditStatus;           // 审核状态筛选
}
```

### 7.5 VO（出参）规范

```java
package com.emiyaoj.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogVO {

    @JsonSerialize(using = ToStringSerializer.class)  // Long → String（防止 JS 精度丢失）
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String authorNickname;         // 作者昵称（非 DB 字段，Service 层填充）
    private String title;
    private String content;
    private Integer blogType;
    private Long problemId;
    private String problemTitle;           // 题目标题（includeProblem=true 时填充）
    private Integer viewCount;
    private Integer likeCount;
    private Boolean liked;                 // 当前用户是否已点赞
    private Integer auditStatus;
    private String auditReason;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<BlogTagVO> tags;          // 关联标签
}
```

### 7.6 对象转换规范

```java
// ✅ 推荐：手动 BeanUtils.copyProperties + 自定义填充
private BlogVO convertToVO(Blog blog) {
    BlogVO vo = new BlogVO();
    BeanUtils.copyProperties(blog, vo);

    // 填充非 DB 字段
    vo.setAuthorNickname(fetchNickname(blog.getUserId()));
    vo.setTags(loadTags(blog.getId()));
    vo.setLiked(checkLiked(blog.getId(), currentUserId));

    return vo;
}

// ❌ 不推荐：直接返回 Entity 给 Controller
```

---

## 8. Feign 远程调用规范

### 8.1 Feign Client 定义（在 `{service}-api` 模块中）

```java
package com.emiyaoj.problem.feign;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.feign.fallback.ProblemFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
    name = "problem-service",              // Nacos 服务名
    path = "/problem",                     // 公共路径前缀
    fallbackFactory = ProblemFeignClientFallback.class
)
public interface ProblemFeignClient {

    @GetMapping("/{id}")
    ResponseResult<ProblemVO> getProblemById(@PathVariable Long id);

    @GetMapping("/test-case/problem/{problemId}")
    ResponseResult<List<TestCaseVO>> getTestCasesByProblemId(@PathVariable Long problemId);

    @GetMapping("/batch")
    ResponseResult<List<ProblemVO>> batchGetProblems(@RequestParam List<Long> ids);
}
```

### 8.2 Feign 降级实现

```java
package com.emiyaoj.problem.feign.fallback;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.feign.ProblemFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ProblemFeignClientFallback implements FallbackFactory<ProblemFeignClient> {

    @Override
    public ProblemFeignClient create(Throwable cause) {
        log.error("ProblemFeignClient 调用失败", cause);
        return new ProblemFeignClient() {
            @Override
            public ResponseResult<ProblemVO> getProblemById(Long id) {
                return ResponseResult.fail("题目服务暂不可用");
            }

            @Override
            public ResponseResult<List<TestCaseVO>> getTestCasesByProblemId(Long problemId) {
                return ResponseResult.success(Collections.emptyList());
            }

            @Override
            public ResponseResult<List<ProblemVO>> batchGetProblems(List<Long> ids) {
                return ResponseResult.success(Collections.emptyList());
            }
        };
    }
}
```

### 8.3 Feign 调用端配置

```java
// 在调用方启动类添加
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.emiyaoj.problem.feign")  // 扫描 Feign 接口
public class JudgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeApplication.class, args);
    }
}
```

### 8.4 Feign 调用规范

| 规范 | 说明 |
|------|------|
| 返回值统一包裹 | 所有 Feign 接口返回值必须用 `ResponseResult<T>` 包裹 |
| 必须有降级 | 每个 Feign Client 必须有 `fallbackFactory` |
| DTO 放 api/dto 模块 | Feign 方法签名中的参数和返回值类型必须在 dto 模块中 |
| 避免循环依赖 | Feign 只能单向调用，不能 A→B 且 B→A |
| 超时配置 | 合理设置 `connectTimeout` 和 `readTimeout` |

### 8.5 Feign 配置示例

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 3000
            read-timeout: 10000
          judge-service:              # 判题服务耗时较长
            read-timeout: 60000
```

---

## 9. 异常处理规范

### 9.1 异常类体系

```java
// 基础业务异常
package com.emiyaoj.common.exception;

public class BaseException extends RuntimeException {
    private final int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}

// 请求参数异常
public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(400, message);
    }
}

// 认证异常
public class CustomerAuthenticationException extends BaseException {
    public CustomerAuthenticationException(String message) {
        super(401, message);
    }
}
```

### 9.2 全局异常处理器

```java
package com.emiyaoj.common.handler;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return ResponseResult.fail(400, msg);
    }

    /** 业务异常 */
    @ExceptionHandler(BaseException.class)
    public ResponseResult<Void> handleBaseException(BaseException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    /** 未知异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return ResponseResult.fail(500, "服务器内部错误");
    }
}
```

### 9.3 异常抛出规范

```java
// ✅ Service 层抛出语义化异常
if (problem == null) {
    throw new BadRequestException("题目不存在");
}

if (!hasPermission) {
    throw new BaseException(403, "无操作权限");
}

// ✅ Controller 层无需 try-catch（由 GlobalExceptionHandler 统一处理）
// ❌ 禁止在 Controller 中吞掉异常
```

---

## 10. API 响应规范

### 10.1 ResponseResult 统一响应体

```java
package com.emiyaoj.common.domain;

import lombok.Data;

@Data
public class ResponseResult<T> {
    private int code;
    private String message;
    private T data;

    // 成功 — 无数据
    public static <T> ResponseResult<T> success() {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.message = "操作成功";
        return result;
    }

    // 成功 — 有数据
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    // 失败
    public static <T> ResponseResult<T> fail(String message) {
        return fail(400, message);
    }

    public static <T> ResponseResult<T> fail(int code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
```

### 10.2 PageVO 统一分页响应

```java
package com.emiyaoj.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageVO<T> {
    private List<T> list;          // 当前页数据
    private Long total;            // 总记录数
    private Long pageNum;          // 当前页码
    private Long pageSize;         // 每页条数

    /** 无类型转换 */
    public static <T> PageVO<T> of(Page<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.list = page.getRecords();
        vo.total = page.getTotal();
        vo.pageNum = page.getCurrent();
        vo.pageSize = page.getSize();
        return vo;
    }

    /** 带类型转换（Entity → VO） */
    public static <P, T> PageVO<T> of(Page<P> page, Function<P, T> converter) {
        PageVO<T> vo = new PageVO<>();
        vo.list = page.getRecords().stream().map(converter).collect(Collectors.toList());
        vo.total = page.getTotal();
        vo.pageNum = page.getCurrent();
        vo.pageSize = page.getSize();
        return vo;
    }
}
```

### 10.3 HTTP 状态码约定

| HTTP Status | 含义 | 使用场景 |
|-------------|------|---------|
| 200 | 成功 | `ResponseResult.success()` |
| 400 | 请求参数错误 | `@Valid` 校验失败 / `BadRequestException` |
| 401 | 未认证 | Token 缺失/过期（Gateway 层返回） |
| 403 | 无权限 | 权限不足 |
| 404 | 资源不存在 | 查询/删除不存在的记录 |
| 500 | 服务器错误 | 未知异常 |

---

## 11. 配置管理规范

### 11.1 多环境配置策略

```yaml
# application.yml — 公共配置
spring:
  application:
    name: problem-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# application-dev.yml — 本地开发
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
  datasource:
    url: jdbc:mysql://localhost:3306/emiya_oj_problem
    username: root
    password: root

---
# application-docker.yml — Docker 环境（通过环境变量注入敏感信息）
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:nacos:8848}
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:mysql}:3306/emiya_oj_problem
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
```

### 11.2 敏感配置处理

```yaml
# ❌ 禁止硬编码密钥
jwt:
  secret-key: MySuperSecretKey2024!@#$    # 禁止！

# ✅ 通过环境变量注入
jwt:
  secret-key: ${JWT_SECRET_KEY}           # 编译期不暴露

# ✅ Docker Compose 中注入
services:
  gateway:
    environment:
      JWT_SECRET_KEY: ${JWT_SECRET_KEY}   # 从 .env 文件读取
```

### 11.3 Nacos 配置中心使用

```
# Nacos 中配置（Data ID: problem-service.yaml）
problem:
  page:
    max-page-size: 100

# 代码中读取
@RefreshScope                     // 支持热更新
@RestController
public class ProblemController {

    @Value("${problem.page.max-page-size}")
    private int maxPageSize;
}
```

---

## 12. 安全规范

### 12.1 认证流程

```
请求 → Gateway (AuthGlobalFilter)
         │
         ├── 白名单路径？ → 直接放行
         │
         ├── 提取 Bearer Token
         │
         ├── JwtUtil.parseJWT() 解析 Token
         │
         ├── Redis 验证 Token 白名单
         │
         └── 注入请求头：X-User-Id, X-User-Name, X-User-Roles
              │
              ↓
         下游微服务（从请求头获取用户信息）
```

### 12.2 密码安全

```java
// ✅ 使用 BCrypt 加密
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// ✅ Service 中使用
user.setPassword(passwordEncoder.encode(saveDTO.getPassword()));

// ❌ 禁止明文存储密码
// ❌ 禁止使用 MD5/SHA1 存储密码
```

### 12.3 SQL 注入防护

```java
// ✅ MyBatis-Plus 自动防注入（参数化查询）
wrapper.eq("username", username);   // → WHERE username = ? （参数化）

// ❌ 禁止字符串拼接 SQL
String sql = "SELECT * FROM user WHERE username = '" + username + "'";  // 禁止！

// ✅ 如果必须手写 SQL，使用 #{} 而非 ${}
// MyBatis XML: WHERE username = #{username}
```

### 12.4 权限校验

```java
// ✅ Service 层做权限判断
if (!hasPermission(userId, "problem:delete")) {
    throw new BaseException(403, "无删除权限");
}

// ✅ 需要同时判断多个条件时提取为方法
private void checkOwnershipOrAdmin(Long resourceOwnerId, Long currentUserId, String perm) {
    boolean isOwner = resourceOwnerId.equals(currentUserId);
    boolean isAdmin = hasPermission(currentUserId, perm);
    if (!isOwner && !isAdmin) {
        throw new BaseException(403, "无权操作该资源");
    }
}
```

---

## 13. 数据库规范

### 13.1 表命名规范

| 对象 | 规范 | 示例 |
|------|------|------|
| 数据库 | `emiya_oj_{模块}` | `emiya_oj_auth`, `emiya_oj_blog` |
| 表名 | 小写蛇形命名（snake_case） | `user`, `blog_comment`, `contest_registration` |
| 主键 | `id` | `id BIGINT` |
| 关联表 | `{表A}_{表B}` | `user_role`, `problem_tag` |
| 索引 | `idx_{表}_{字段}` | `idx_user_username` |
| 唯一索引 | `uk_{表}_{字段}` | `uk_user_email` |

### 13.2 通用字段约定

```sql
-- 每张业务表必须包含
id          BIGINT      PRIMARY KEY     -- 雪花算法，禁止自增
deleted     TINYINT     DEFAULT 0       -- 逻辑删除：0-未删, 1-已删
create_time DATETIME    DEFAULT NOW()   -- 创建时间
update_time DATETIME    DEFAULT NOW()   -- 更新时间
create_by   BIGINT                      -- 创建人（可选）
update_by   BIGINT                      -- 更新人（可选）

-- 状态类字段约定
status      TINYINT     DEFAULT 1       -- 0-禁用/隐藏, 1-启用/公开
```

### 13.3 MyBatis-Plus 实体映射规范

```java
@Data
@TableName("problem")
public class Problem {

    @TableId(type = IdType.ASSIGN_ID)        // 雪花 ID
    private Long id;

    private String title;
    private Integer difficulty;

    @TableLogic(value = "0", delval = "1")   // 逻辑删除
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)      // 自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 13.4 索引规范

```sql
-- ✅ 高频查询字段建索引
CREATE INDEX idx_submission_user_id ON submission(user_id);
CREATE INDEX idx_submission_problem_id ON submission(problem_id);

-- ✅ 联合唯一索引防止重复
CREATE UNIQUE INDEX uk_user_role ON user_role(user_id, role_id);

-- ✅ 外键字段建索引
CREATE INDEX idx_blog_comment_blog_id ON blog_comment(blog_id);
```

---

## 14. Git 提交规范

### 14.1 Conventional Commits

```
<type>(<scope>): <subject>

<body>
```

### 14.2 Type 与 Scope

| Type | 说明 | Scope 示例 |
|------|------|-----------|
| `feat` | 新功能 | `auth`, `judge`, `blog` |
| `fix` | 修复 Bug | `gateway`, `problem` |
| `refactor` | 代码重构 | `common`, `moderation` |
| `docs` | 文档变更 | — |
| `style` | 代码格式 | — |
| `test` | 测试 | — |
| `chore` | 构建/依赖 | `maven`, `docker` |

### 14.3 提交示例

```
feat(judge): 实现异步判题引擎与 Go-Judge 集成

- 新增 JudgeExecutor 异步判题执行器
- 新增 GoJudgeService HTTP 通信层
- 新增本地消息表保证判题结果最终一致性
- 支持编译超时和运行超时独立控制

Closes #23
```

---

## 15. 代码质量规范

### 15.1 注解使用标准

| 类别 | 注解 | 何时使用 |
|------|------|---------|
| Spring | `@RestController` | 所有 Controller |
| | `@Service` | 所有 Service 实现 |
| | `@Component` | 通用组件（拦截器、过滤器等） |
| | `@Configuration` | 配置类 |
| Lombok | `@Data` | Entity / DTO / VO |
| | `@RequiredArgsConstructor` | 需要构造器注入的类 |
| | `@Slf4j` | 需要日志的类 |
| MyBatis-Plus | `@TableName` | Entity |
| | `@TableId(type = ASSIGN_ID)` | Entity 主键 |
| | `@TableLogic` | Entity 逻辑删除字段 |
| Swagger | `@Tag(name = "...")` | Controller 类 |
| | `@Operation(summary = "...")` | Controller 方法 |
| 校验 | `@Valid` | Controller 入参 |
| | `@NotBlank`, `@NotNull`, `@Size` | DTO 字段 |

### 15.2 编码风格

```java
// ✅ 推荐的代码风格

// 1. 构造器注入（@RequiredArgsConstructor 代替 @Autowired）
@RestController
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService problemService;
}

// 2. 使用 Lambda 表达式
List<ProblemVO> vos = problems.stream()
        .map(this::convertToVO)
        .collect(Collectors.toList());

// 3. 使用 Optional 避免 NPE
Optional.ofNullable(user).map(User::getNickname).orElse("未知用户");

// 4. 链式调用（LambdaQueryWrapper）
wrapper.eq(StringUtils.hasText(title), Problem::getTitle, title)
       .orderByDesc(Problem::getCreateTime);
```

### 15.3 日志规范

```java
@Slf4j
public class ProblemService {

    public void saveProblem(ProblemSaveDTO dto, Long userId) {
        // ✅ 使用占位符而非字符串拼接
        log.info("用户 {} 创建题目: {}", userId, dto.getTitle());

        // ✅ 异常必须记录完整堆栈
        try {
            // ...
        } catch (Exception e) {
            log.error("保存题目失败, title={}", dto.getTitle(), e);  // 第三个参数是 Throwable
            throw new BaseException(500, "保存题目失败");
        }

        // ✅ 调试信息用 debug
        log.debug("题目详情: id={}, tags={}", problem.getId(), problem.getTags());
    }
}
```

### 15.4 代码审查检查清单

| 检查项 | 说明 |
|--------|------|
| Controller 无业务逻辑 | 只做参数接收和结果返回 |
| Service 事务完整 | 写操作正确的 `@Transactional(rollbackFor = Exception.class)` |
| Entity 不泄漏到 Controller | 只返回 VO |
| DTO 参数校验 | `@Valid` + JSR-303 注解 |
| Feign 有降级 | 每个 `@FeignClient` 有 `fallbackFactory` |
| 异常不吞掉 | catch 后要么处理、要么转换后抛出 |
| SQL 无注入风险 | 使用 MyBatis-Plus 参数化查询 |
| 密码已加密 | BCrypt 加密存储 |
| 日志不输出敏感信息 | 密码、Token 等禁止 log |
| 响应统一格式 | 所有 API 返回 `ResponseResult` |

---

> **文档维护**: 本规范基于 EmiyaOJ-Cloud 项目实践总结。与项目共同演进，Code Review 中发现的问题应及时补充到规范中。
