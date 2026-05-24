# Profile Center API

本文档记录 OJ 个人中心相关接口。所有 JSON 接口统一返回 `ResponseResult<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 数据口径

| 指标 | 口径 |
| --- | --- |
| `totalSubmitCount` | 用户未删除提交总次数 |
| `acceptedSubmitCount` | 最终判题状态为 `AC(2)` 的提交次数 |
| `solvedCount` | 按 `userId + problemId` 去重后的 AC 题目数，仅展示公开且未删除题目 |
| `passRate` | `acceptedSubmitCount / totalSubmitCount * 100`，保留两位小数 |
| `difficultyStats` | 按题目难度 `1-简单, 2-中等, 3-困难` 统计已解决题目数 |
| `blogCount` | 用户已审核通过且未删除的博客数 |
| `starCount` | 用户收藏的已审核通过且未删除博客数 |
| `likedBlogCount` | 用户点赞的已审核通过且未删除博客数 |

## GET `/user/center/me`

查询当前登录用户个人中心总览。

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | 当前登录用户 ID，由网关注入 |

Response `data`: `ProfileCenterVO`

```json
{
  "user": {
    "id": 1001,
    "username": "emiya",
    "nickname": "卫宫",
    "avatar": "https://example.com/avatar.png",
    "createTime": "2026-05-01T12:00:00"
  },
  "solvedCount": 8,
  "totalSubmitCount": 30,
  "acceptedSubmitCount": 12,
  "passRate": 40.00,
  "difficultyStats": [
    { "difficulty": 1, "difficultyDesc": "简单", "solvedCount": 5 },
    { "difficulty": 2, "difficultyDesc": "中等", "solvedCount": 2 },
    { "difficulty": 3, "difficultyDesc": "困难", "solvedCount": 1 }
  ],
  "blogCount": 3,
  "starCount": 6,
  "likedBlogCount": 9
}
```

## GET `/user/center/{userId}`

查询指定用户公开个人中心总览。返回字段同 `/user/center/me`。公开用户信息只包含 `id`、`username`、`nickname`、`avatar`、`createTime`，不会返回邮箱、手机号、角色、权限。

## GET `/submission/user/{userId}/stats`

查询用户判题统计，供个人中心聚合使用，也可直接用于刷题统计卡片。

Response `data`: `JudgeUserStatsVO`

```json
{
  "userId": 1001,
  "solvedCount": 8,
  "totalSubmitCount": 30,
  "acceptedSubmitCount": 12,
  "passRate": 40.00,
  "difficultyStats": [
    { "difficulty": 1, "difficultyDesc": "简单", "solvedCount": 5 },
    { "difficulty": 2, "difficultyDesc": "中等", "solvedCount": 2 },
    { "difficulty": 3, "difficultyDesc": "困难", "solvedCount": 1 }
  ]
}
```

## GET `/submission/user/{userId}/solved`

分页查询用户已解决题目，支持按难度过滤。

Query:

| Name | Required | Description |
| --- | --- | --- |
| `pageNum` | no | 页码，默认 `1` |
| `pageSize` | no | 每页条数，默认 `10` |
| `difficulty` | no | `1` 简单，`2` 中等，`3` 困难 |

Response `data`: `PageVO<SolvedProblemVO>`

```json
{
  "total": 1,
  "list": [
    {
      "problemId": 1,
      "title": "A+B",
      "difficulty": 1,
      "difficultyDesc": "简单",
      "acceptedAt": "2026-05-01T12:03:00"
    }
  ],
  "pageNum": 1,
  "pageSize": 10
}
```

`acceptedAt` 为该用户该题最早 AC 时间。

## GET `/problem/internal/batch`

内部批量查询公开题目信息，供判题服务补齐已解决题目的标题和难度。

Query:

| Name | Required | Description |
| --- | --- | --- |
| `ids` | yes | 题目 ID 列表，如 `ids=1,2,3` 或重复传参 |

Response `data`: `List<ProblemVO>`。仅返回未删除且 `status=1` 的题目。

## POST `/blog/user/{uid}/blogs/query`

分页查询用户自己写的博客。该接口已存在，个人中心继续复用。

Request body:

```json
{
  "pageNo": 1,
  "pageSize": 10,
  "auditStatus": 1
}
```

Response `data`: `PageVO<BlogVO>`。

## POST `/blog/user/{uid}/stars/query`

分页查询用户收藏的博客。该接口已存在，个人中心继续复用。

Request body:

```json
{
  "pageNo": 1,
  "pageSize": 10
}
```

Response `data`: `PageVO<BlogVO>`。

## POST `/blog/user/{uid}/likes/query`

分页查询用户点赞过的博客。

Request body:

```json
{
  "pageNo": 1,
  "pageSize": 10
}
```

Response `data`: `PageVO<BlogVO>`。仅返回未删除且审核通过的博客。

## GET `/blog/internal/user/{userId}/stats`

内部查询用户博客统计，供个人中心聚合使用。

Response `data`: `BlogUserStatsVO`

```json
{
  "userId": 1001,
  "blogCount": 3,
  "starCount": 6,
  "likedBlogCount": 9
}
```

## DTO Fields

`ProfileCenterVO`:

| Field | Type | Description |
| --- | --- | --- |
| `user` | PublicUserVO | 公开用户信息 |
| `solvedCount` | Integer | 已解决题目数量 |
| `totalSubmitCount` | Integer | 总提交次数 |
| `acceptedSubmitCount` | Integer | AC 提交次数 |
| `passRate` | BigDecimal | 通过率百分比 |
| `difficultyStats` | List | 按难度统计已解决题目 |
| `blogCount` | Integer | 已通过博客数 |
| `starCount` | Integer | 收藏博客数 |
| `likedBlogCount` | Integer | 点赞博客数 |

`SolvedProblemVO`:

| Field | Type | Description |
| --- | --- | --- |
| `problemId` | Long | 题目 ID |
| `title` | String | 题目标题 |
| `difficulty` | Integer | 难度 |
| `difficultyDesc` | String | 难度描述 |
| `acceptedAt` | LocalDateTime | 最早 AC 时间 |
