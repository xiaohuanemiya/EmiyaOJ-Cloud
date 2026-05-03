# Blog API

All JSON endpoints return `ResponseResult<T>`:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## Rules

`blogType`:

| Value | Meaning |
| --- | --- |
| `0` | Normal blog |
| `1` | Problem solution |

`sortBy`: `createTime` default, `updateTime`, `viewCount`, or `likeCount`.

Images are stored in MinIO. `POST /blog/images` returns a public `url`, which can be embedded directly in Markdown or rich text. The download endpoint is provided for explicit file download.

Each user can have only one solution for the same problem. Creating a second solution for the same problem updates the previous solution instead of creating another record.

## POST `/blog`

Create a normal blog. To create a problem solution, prefer `POST /blog/problems/{problemId}/solutions`.

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Request body:

```json
{
  "title": "My blog",
  "content": "Markdown content with ![](http://127.0.0.1:9000/blog-images/blog/1/202605/a.png)",
  "blogType": 0,
  "tagIds": [1, 2],
  "pictureIds": [10]
}
```

Response `data`: `null`.

## POST `/blog/problems/{problemId}/solutions`

Create or update the current user's solution for a problem.

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Path:

| Name | Required | Description |
| --- | --- | --- |
| `problemId` | yes | Linked problem ID |

Request body:

```json
{
  "title": "Two pointers solution",
  "content": "Solution content",
  "tagIds": [1],
  "pictureIds": [10, 11]
}
```

Response `data`: `null`.

## POST `/blog/query`

Query blogs.

Optional header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | no | Used to populate `BlogVO.liked` |

Request body:

```json
{
  "title": "solution",
  "blogType": 1,
  "problemId": 1001,
  "tagId": 1,
  "sortBy": "likeCount",
  "createTime": "2026-05-04T00:00:00",
  "pageNo": 1,
  "pageSize": 10
}
```

Response `data`: `PageVO<BlogVO>`.

## POST `/blog/problems/{problemId}/solutions/query`

Query the solution list for one problem. The service forces `blogType=1` and `problemId` from the path.

Request body:

```json
{
  "sortBy": "viewCount",
  "pageNo": 1,
  "pageSize": 10
}
```

Response `data`: `PageVO<BlogVO>`.

## GET `/blog/{bid}`

Get blog detail. If `X-User-Id` exists, the service increments view count at most once per user per 24 hours and returns whether the user has liked the blog.

Response `data`: `BlogVO`.

## POST `/blog/{bid}/like`

Like a blog. Duplicate likes are treated as success.

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Response `data`: `null`.

## DELETE `/blog/{bid}/like`

Cancel a like. Duplicate cancels are treated as success.

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Response `data`: `null`.

## POST `/blog/images`

Upload an image.

Content type: `multipart/form-data`

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Form:

| Name | Required | Description |
| --- | --- | --- |
| `file` | yes | `jpg/jpeg/png/webp/gif`, max 10 MB |

Response `data`: `BlogPictureVO`.

```json
{
  "id": "10",
  "blogId": null,
  "url": "http://127.0.0.1:9000/blog-images/blog/1/202605/uuid.png",
  "contentType": "image/png",
  "size": 20480,
  "originalFilename": "diagram.png",
  "createTime": "2026-05-04T12:00:00"
}
```

## GET `/blog/images/{id}/download`

Download an uploaded image.

Response: binary stream with `Content-Type` and `Content-Disposition`.

## DELETE `/blog/images/{id}`

Delete an uploaded image. Only the uploader can delete it.

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Response `data`: `null`.

## DTO Fields

`BlogSaveDTO`:

| Field | Type | Description |
| --- | --- | --- |
| `title` | String | Max 50 |
| `content` | String | Max 10000 |
| `blogType` | Integer | `0` normal blog, `1` solution. Optional for normal blog |
| `problemId` | Long | Required when `blogType=1`; path value is used by solution endpoint |
| `tagIds` | List<Long> | Optional tag IDs |
| `pictureIds` | List<Long> | Optional uploaded image IDs to bind |

`BlogQueryDTO`:

| Field | Type | Description |
| --- | --- | --- |
| `title` | String | Fuzzy title filter |
| `blogType` | Integer | `0` normal blog, `1` solution |
| `problemId` | Long | Linked problem ID |
| `tagId` | Long | Filter by blog tag |
| `sortBy` | String | `createTime`, `updateTime`, `viewCount`, `likeCount` |
| `createTime` | LocalDateTime | Filter blogs created on the same date |
| `pageNo` | Integer | Page number |
| `pageSize` | Integer | Page size |

`BlogVO`:

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long as string | Blog ID |
| `userId` | Long as string | Author ID |
| `title` | String | Title |
| `content` | String | Content |
| `blogType` | Integer | `0` normal blog, `1` solution |
| `problemId` | Long as string | Linked problem ID for solutions |
| `problemTitle` | String | Linked problem title on detail responses |
| `viewCount` | Integer | View count |
| `likeCount` | Integer | Like count |
| `liked` | Boolean | Whether current user liked this blog |
| `createTime` | LocalDateTime | Create time |
| `updateTime` | LocalDateTime | Update time |
| `tags` | List<BlogTagVO> | Blog tags |
| `pictures` | List<BlogPictureVO> | Bound images |

`BlogPictureVO`:

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long as string | Image ID |
| `blogId` | Long as string | Bound blog ID, or `null` before binding |
| `url` | String | Public image URL |
| `contentType` | String | MIME type |
| `size` | Long | File size in bytes |
| `originalFilename` | String | Original filename |
| `createTime` | LocalDateTime | Upload time |
