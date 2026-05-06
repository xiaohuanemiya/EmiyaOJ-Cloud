# 博客文本审核接口说明

本文档记录本次博客文本审核改造新增的接口，以及已有接口中与审核状态相关的参数和返回字段变化。

## 审核状态

`auditStatus` 统一使用整数值：

| 值 | 状态 | 含义 |
| --- | --- | --- |
| `0` | `PENDING` | 待审核，默认隐藏 |
| `1` | `APPROVED` | 审核通过，可公开展示 |
| `2` | `REJECTED` | 审核驳回，默认隐藏 |
| `3` | `MANUAL_REVIEW` | 需要人工复核，默认隐藏 |

普通公开查询默认只返回 `APPROVED` 内容。作者可以查看自己的待审、驳回、人工复核内容；管理员需要具备权限编码 `BLOG_MODERATION_MANAGE`。

## 新增接口

### 审核结果回写

该接口仅供 `EmiyaOJ-Moderation` 服务内部调用，前端和外部客户端不应直接调用。

```http
POST /blog/internal/moderation/result
```

Headers：

| Header | 必填 | 说明 |
| --- | --- | --- |
| `X-Moderation-Token` | 是 | 内部服务调用令牌，值来自环境变量 `MODERATION_INTERNAL_TOKEN` |

Request Body：

```json
{
  "taskId": "8f4c8c7f-77d8-4c86-a6a2-3d5e3f6a4f2a",
  "targetType": "BLOG",
  "targetId": "10001",
  "auditStatus": 1,
  "suggestion": "pass",
  "labels": "abuse,politics",
  "reason": "suggestion=pass; labels=abuse,politics",
  "auditTime": "2026-05-06T13:58:00"
}
```

字段说明：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `taskId` | 是 | 审核任务 ID，必须与当前博客或评论的 `audit_task_id` 一致 |
| `targetType` | 是 | 审核对象类型：`BLOG` 或 `COMMENT` |
| `targetId` | 是 | 博客 ID 或评论 ID |
| `auditStatus` | 是 | 审核结果状态：`1`、`2`、`3` |
| `suggestion` | 否 | 阿里云建议结果，例如 `pass`、`review`、`block` |
| `labels` | 否 | 命中的风险标签，逗号分隔 |
| `reason` | 否 | 审核原因或异常信息 |
| `auditTime` | 否 | 审核完成时间 |

说明：

- 服务端会校验 `taskId`，旧任务结果不会覆盖编辑后的新内容。
- 重复回写保持幂等。
- `X-Moderation-Token` 不应提交到 Git，可通过环境变量配置。

### 人工更新博客审核状态

```http
PUT /blog/moderation/blogs/{bid}/status?auditStatus=1&reason=人工复核通过
```

Headers：

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 用户登录凭证 |
| `X-User-Roles` | 是 | 需要包含 `BLOG_MODERATION_MANAGE` |

Path Variables：

| 字段 | 说明 |
| --- | --- |
| `bid` | 博客 ID |

Query Params：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `auditStatus` | 是 | 目标状态，只允许 `1=APPROVED`、`2=REJECTED`、`3=MANUAL_REVIEW` |
| `reason` | 否 | 人工处理原因 |

### 人工更新评论审核状态

```http
PUT /blog/moderation/comments/{cid}/status?auditStatus=2&reason=包含违规内容
```

Headers、Query Params 与人工更新博客审核状态一致。

Path Variables：

| 字段 | 说明 |
| --- | --- |
| `cid` | 评论 ID |

## 已有接口变化

### 发布或编辑内容

以下接口保存成功后，内容会进入 `PENDING` 状态并异步投递文本审核任务：

```http
POST /blog
PUT /blog/{bid}
POST /blog/{bid}/comments
```

在审核通过前，普通公开查询不会展示这些内容。

### 查询参数新增 auditStatus

以下查询 DTO 新增可选字段 `auditStatus`：

```http
POST /blog/query
POST /blog/user/{uid}/blogs/query
POST /blog/{bid}/comments/query
POST /blog/comments/query
```

规则：

- 不传 `auditStatus` 时，普通公开查询只返回 `APPROVED`。
- 作者查询自己的内容时，可以按 `auditStatus` 查看待审、驳回或人工复核内容。
- 管理员携带 `BLOG_MODERATION_MANAGE` 权限时，可以按 `auditStatus` 查询全部内容。

### 返回字段新增

`BlogVO` 和 `CommentVO` 新增：

```json
{
  "auditStatus": 1,
  "auditReason": "suggestion=pass; labels="
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `auditStatus` | 审核状态 |
| `auditReason` | 审核原因、命中规则或人工处理原因 |

## 部署配置

审核服务、RabbitMQ、阿里云 AK/SK、内部调用令牌的配置方式见：

```text
docs/Moderation-Setup.md
```

当前版本只审核文本，不审核图片或正文中的图片链接。
