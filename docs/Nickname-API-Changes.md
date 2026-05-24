# 用户昵称展示接口变更说明

本文档记录本次为博客与题单展示用户昵称而新增、修改的接口契约。

所有 JSON 接口仍返回统一结构 `ResponseResult<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 新增接口

### GET `/user/batch`

批量查询用户公开展示信息。该接口供 Blog Service、Problem Service 通过 Feign 获取昵称，避免列表页逐条查询用户信息。

该接口只返回公开展示字段，不返回邮箱、手机号、角色、权限等敏感或管理信息。

认证要求：无需登录。

Query 参数：

| Name | Required | Type | Description |
| --- | --- | --- | --- |
| `ids` | yes | `List<Long>` | 用户 ID 列表，例如 `ids=1,2,3` |

Response `data`: `List<UserVO>`。

返回字段：

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long as string | 用户 ID |
| `username` | String | 用户名 |
| `nickname` | String | 昵称 |
| `avatar` | String | 头像 URL |

示例：

```http
GET /user/batch?ids=2051888299500380162,2051898437005271041
```

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "2051888299500380162",
      "username": "admintestuser",
      "nickname": "Admin Test User",
      "avatar": null
    }
  ]
}
```

## 修改接口

### 博客列表与详情

以下接口返回的 `BlogVO` 新增 `authorNickname` 字段：

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/blog` | 查询全部博客 |
| `POST` | `/blog/query` | 分页查询博客 |
| `GET` | `/blog/{bid}` | 查询博客详情 |
| `POST` | `/blog/problems/{problemId}/solutions/query` | 查询题解列表 |
| `POST` | `/blog/user/{uid}/blogs/query` | 查询指定用户发布的博客 |
| `POST` | `/blog/user/{uid}/stars/query` | 查询指定用户收藏的博客 |

`BlogVO` 新增字段：

| Field | Type | Description |
| --- | --- | --- |
| `authorNickname` | String | 作者昵称，来源于 Auth Service 的用户昵称 |

补充说明：

- `authorNickname` 查询失败、用户不存在或昵称为空时返回空字符串 `""`。
- `userId` 字段保持不变。
- 博客作者昵称不再读取 `user_blog.nickname`。

示例片段：

```json
{
  "id": "1001",
  "userId": "2051888299500380162",
  "authorNickname": "Admin Test User",
  "title": "Two pointers solution",
  "blogType": 1
}
```

### 博客用户资料

`GET /blog/user/{uid}` 保持原路径和响应类型 `UserBlogVO` 不变，但数据来源调整。

返回字段：

| Field | Type | Description |
| --- | --- | --- |
| `userId` | Long as string | 用户 ID |
| `username` | String | 用户名，来源于 Auth Service |
| `nickname` | String | 昵称，来源于 Auth Service |
| `blogCount` | Integer | 已审核通过且未删除的博客数，实时从 `blog` 表统计 |
| `starCount` | Integer | 未取消的收藏数，实时从 `blog_star` 表统计 |

补充说明：

- 该接口不再创建或读取 `user_blog` 记录。
- `username`、`nickname` 查询失败时返回空字符串 `""`。

### 博客评论

博客评论接口响应结构不变，`CommentVO.username` 与 `CommentVO.nickname` 的数据来源调整为 Auth Service。

受影响接口：

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/blog/{bid}/comments/query` | 查询博客评论分页 |
| `POST` | `/blog/comments/query` | 查询评论列表 |
| `GET` | `/blog/comments/{cid}` | 查询评论详情 |

补充说明：

- 评论用户昵称不再读取 `user_blog.nickname`。
- 查询失败、用户不存在或昵称为空时，`username`、`nickname` 返回空字符串 `""`。

### 题单列表与详情

以下接口返回的 `ProblemSetVO` 新增 `creatorNickname` 字段：

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/problem-set/list` | 查询题单列表 |
| `GET` | `/problem-set/{id}` | 查询题单详情 |
| `POST` | `/problem-set` | 创建题单后返回题单详情 |

`ProblemSetVO` 新增字段：

| Field | Type | Description |
| --- | --- | --- |
| `creatorNickname` | String | 创建者昵称，来源于 Auth Service 的用户昵称 |

补充说明：

- `creatorNickname` 查询失败、用户不存在或昵称为空时返回空字符串 `""`。
- `creatorId` 字段保持不变。

示例片段：

```json
{
  "id": 1,
  "title": "Dynamic Programming",
  "creatorId": 2051888299500380162,
  "creatorNickname": "Admin Test User",
  "status": 1,
  "problemCount": 12
}
```

## 兼容性说明

- 本次变更只新增响应字段，不删除原有字段。
- 前端原有按 `userId`、`creatorId` 展示或跳转的逻辑不受影响。
- `user_blog` 表与相关实体暂时保留，但昵称展示与用户博客统计已不再依赖该表。
