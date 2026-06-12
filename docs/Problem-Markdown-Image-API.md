# 题目 Markdown 与图片上传接口变更文档

## 1. 变更说明

题目服务现已支持 Markdown 题面和题目图片上传。

- `description`、`inputDescription`、`outputDescription`、`hint` 均按 Markdown 文本保存和返回。
- 前端负责使用 Markdown 渲染组件展示题面。
- 图片上传成功后，前端将返回的图片 URL 插入 Markdown，并在保存题目时通过 `pictureIds` 绑定图片。
- 图片元数据由题目服务独立管理，不复用博客图片接口。

网关地址示例：

```text
http://localhost:8080
```

通过网关调用本文新增接口时，均需携带登录令牌：

```http
Authorization: Bearer <token>
```

统一响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 2. 新增图片接口

### 2.1 上传题目图片

`POST /problem/images`

请求类型：

```http
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | File | 是 | 图片文件 |

支持格式：

```text
jpg、jpeg、png、webp、gif
```

文件大小限制：

```text
不超过 10 MB
```

成功响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "12",
    "problemId": null,
    "url": "http://127.0.0.1:9000/problem-images/problem/10001/202605/7d48d04d-47a0-46ff-b844-d89aa7d6239d.png",
    "contentType": "image/png",
    "size": 25843,
    "originalFilename": "diagram.png",
    "createTime": "2026-05-30T10:20:30"
  }
}
```

前端处理建议：

1. 保留返回的 `data.id`，加入当前编辑表单的 `pictureIds`。
2. 将以下 Markdown 插入编辑器光标位置：

```md
![diagram.png](http://127.0.0.1:9000/problem-images/problem/10001/202605/7d48d04d-47a0-46ff-b844-d89aa7d6239d.png)
```

常见错误：

| HTTP/业务码 | 说明 |
| --- | --- |
| `400` | 图片为空、格式不支持或超过 10 MB |
| `500` | MinIO 对象存储不可用 |

### 2.2 下载题目图片

`GET /problem/images/{id}/download`

请求头：

```http
Authorization: Bearer <token>
```

路径参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | String | 是 | 图片 ID |

成功时直接返回图片二进制流，并携带对应的 `Content-Type`。

说明：Markdown 展示优先直接使用上传接口返回的公开 `url`。下载接口适用于显式下载场景。

### 2.3 删除题目图片

`DELETE /problem/images/{id}`

请求头：

```http
Authorization: Bearer <token>
```

路径参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | String | 是 | 图片 ID |

成功响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

限制：

- 仅允许删除当前用户自己上传的图片。
- 删除后应同步从编辑器 Markdown 和表单 `pictureIds` 中移除该图片。

## 3. 题目保存接口变更

### 3.1 新增题目

`POST /problem`

### 3.2 更新题目

`PUT /problem`

两个接口的请求体 `ProblemSaveDTO` 新增字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pictureIds` | Array\<String\> | 否 | 当前题目需要绑定的图片 ID 列表 |

请求示例：

```json
{
  "id": 1001,
  "title": "A + B Problem",
  "description": "计算两个整数之和。\n\n![示意图](http://127.0.0.1:9000/problem-images/problem/10001/202605/7d48d04d-47a0-46ff-b844-d89aa7d6239d.png)",
  "inputDescription": "输入两个整数 `a` 和 `b`。",
  "outputDescription": "输出 `a + b`。",
  "sampleInput": "1 2",
  "sampleOutput": "3",
  "hint": "注意整数范围。",
  "difficulty": 1,
  "timeLimit": 1000,
  "memoryLimit": 256,
  "stackLimit": 128,
  "source": "Demo",
  "status": 1,
  "tagIds": [1, 2],
  "pictureIds": ["12"]
}
```

说明：

- 调用 `POST /problem` 新增题目时省略 `id`。
- 调用 `PUT /problem` 更新题目时必须传递 `id`。

更新题目时，`pictureIds` 的语义如下：

| 传值 | 行为 |
| --- | --- |
| 不传或 `null` | 保持原有图片绑定不变 |
| `[]` | 清空当前题目的全部图片绑定 |
| `["12", "13"]` | 将图片绑定替换为指定列表 |

后端校验规则：

- 图片必须存在且未删除。
- 图片必须由当前用户上传。
- 重复图片 ID 会自动去重。

## 4. 题目详情接口变更

`GET /problem/{id}`

题目详情响应新增字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `pictures` | Array\<ProblemPictureVO\> | 当前题目绑定的图片列表 |

`ProblemPictureVO`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | String | 图片 ID，字符串形式返回以避免 JavaScript 精度丢失 |
| `problemId` | String \| null | 已绑定题目 ID；刚上传未保存时为 `null` |
| `url` | String | 可直接用于 Markdown 展示的公开 URL |
| `contentType` | String | MIME 类型 |
| `size` | Number | 文件大小，单位为字节 |
| `originalFilename` | String | 原始文件名 |
| `createTime` | String | 上传时间 |

题面 Markdown 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `description` | String | 题目描述 Markdown |
| `inputDescription` | String | 输入描述 Markdown |
| `outputDescription` | String | 输出描述 Markdown |
| `hint` | String | 提示 Markdown |

后端会在返回详情时兼容重写旧 MinIO 地址。前端应直接渲染接口返回的 Markdown 文本，不要自行拼接对象存储地址。

## 5. 列表接口说明

以下列表类接口不会额外加载题目图片详情：

```text
GET /problem/list
GET /problem/internal/batch
```

列表响应中的 `pictures` 为空数组。进入题目详情页后，再通过 `GET /problem/{id}` 获取完整图片列表。

## 6. 推荐前端流程

### 新增题目

1. 用户在 Markdown 编辑器中选择图片。
2. 调用 `POST /problem/images` 上传。
3. 将返回 URL 作为 Markdown 图片语法插入编辑器。
4. 将返回图片 ID 加入 `pictureIds`。
5. 提交 `POST /problem`。

### 编辑题目

1. 调用 `GET /problem/{id}` 获取题面和 `pictures`。
2. 初始化 Markdown 编辑器内容与 `pictureIds`。
3. 新上传图片时追加 ID。
4. 删除图片时调用 `DELETE /problem/images/{id}`，并移除 Markdown 和 ID。
5. 提交 `PUT /problem`，传递最终的 `pictureIds` 完成替换绑定。

## 7. 前端注意事项

- 图片 ID 按字符串处理，避免 JavaScript `Number` 精度问题。
- 保存题目时建议将 `pictureIds` 作为字符串数组原样提交；后端可转换为 `Long`。
- Markdown 渲染组件需要支持标准图片语法。
- 图片展示 URL 使用上传接口返回值，不要使用下载接口 URL 替代。
- 用户上传图片但未保存题目时，该图片处于未绑定状态；前端可在取消编辑时按需调用删除接口清理。
