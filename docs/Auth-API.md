# Auth API

本文档记录认证服务（登录、注册、登出、Token 解析）接口。所有 JSON 端点统一返回 `ResponseResult<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

## 用户状态码

| Code | Name | Description |
| --- | --- | --- |
| `0` | 禁用 | 账号已被禁用，无法登录 |
| `1` | 启用 | 账号正常可用 |

---

## POST `/auth/register`

用户注册。无需认证，Gateway 白名单放行。

Request body:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `username` | String | **yes** | 用户名，3-50 字符，全局唯一 |
| `password` | String | **yes** | 密码，6-50 字符，BCrypt 加密存储 |
| `nickname` | String | **yes** | 昵称，不超过 50 字符 |
| `email` | String | **yes** | 邮箱，合法格式，全局唯一 |
| `phone` | String | no | 手机号 |

```json
{
  "username": "newuser",
  "password": "123456",
  "nickname": "新用户",
  "email": "newuser@example.com",
  "phone": "13800138000"
}
```

Response `data`: `null`

```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

> **注意**：注册成功后仅创建账号，不返回 Token。用户需另行调用 `POST /auth/login` 登录。新账号默认状态为"启用"（status=1），不分配默认角色。

### 错误码

| 场景 | HTTP Status | message |
| --- | --- | --- |
| 缺少必填字段 | `400` | 参数校验失败（如"用户名不能为空"） |
| 用户名长度不合规 | `400` | 用户名长度为3-50个字符 |
| 密码长度不合规 | `400` | 密码长度为6-50个字符 |
| 邮箱格式错误 | `400` | 邮箱格式不正确 |
| 用户名已存在 | `500` | 用户名已存在 |
| 邮箱已被注册 | `500` | 邮箱已被注册 |

---

## POST `/auth/login`

用户登录。无需认证，Gateway 白名单放行。

Request body:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `username` | String | **yes** | 用户名 |
| `password` | String | **yes** | 密码 |

```json
{
  "username": "newuser",
  "password": "123456"
}
```

Response `data`: `UserLoginVO`

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long | 用户 ID |
| `username` | String | 用户名 |
| `nickname` | String | 昵称 |
| `token` | String | JWT Token（有效期 2 小时） |

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2051898437005271041,
    "username": "newuser",
    "nickname": "新用户",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

> **注意**：登录成功后 Token 自动存入 Redis 白名单（key: `token_{userId}`），后续请求需在 Header 中携带 `Authorization: Bearer <token>`。

### 错误码

| 场景 | HTTP Status | message |
| --- | --- | --- |
| 用户名或密码错误 | `500` | 用户名或密码错误 |
| 账号已被禁用 | `500` | 账号已被禁用 |

---

## POST `/auth/logout`

用户登出，清除 Redis Token 白名单，使当前 Token 立即失效。

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | no | 当前用户 ID（优先使用网关注入的值） |
| `Authorization` | **yes** | Bearer Token（网关解析后注入 X-User-Id） |

Response `data`: `null`

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

> **注意**：登出仅删除 Redis 中的 Token 白名单记录，不会使 JWT 本身过期。网关每次请求会校验 Redis 白名单，因此登出后该 Token 立即不可用。

---

## GET `/auth/user/parse-token`

解析 JWT Token 获取用户认证信息。无需认证，供网关及其他微服务 Feign 调用。

Query parameters:

| Name | Required | Description |
| --- | --- | --- |
| `token` | **yes** | 待解析的 JWT Token 字符串 |

Response `data`: `UserAuthDTO`

| Field | Type | Description |
| --- | --- | --- |
| `userId` | Long | 用户 ID |
| `username` | String | 用户名 |
| `permissions` | List\<String\> | 权限编码列表 |

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 2051898437005271041,
    "username": "newuser",
    "permissions": ["problem:list", "blog:view"]
  }
}
```

### 错误码

| 场景 | HTTP Status | message |
| --- | --- | --- |
| Token 无效或已过期 | `200` | Token 无效或已过期（code=401） |
| Redis 白名单不存在 | `500` | Token 已过期或已注销 |

---

## 接口总览

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | 无需 | 用户注册 |
| `POST` | `/auth/login` | 无需 | 用户登录 |
| `POST` | `/auth/logout` | 需 Token | 用户登出 |
| `GET` | `/auth/user/parse-token` | 无需 | Token 解析（内部/Feign） |
