# Problem Set API

All endpoints return `ResponseResult<T>`:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## Data Model

`problem_set.status`: `0` hidden, `1` public.

Hidden problem sets are visible only to their creator. Public problem sets are visible to logged-in users and anonymous internal callers.

## GET `/problem-set/list`

Query problem sets.

Query:

| Name | Required | Description |
| --- | --- | --- |
| `pageNum` | no | Page number, default `1` |
| `pageSize` | no | Page size, default `10` |
| `title` | no | Fuzzy title filter |
| `status` | no | `0` hidden, `1` public |
| `creatorId` | no | Creator user ID |

Response `data`: `PageVO<ProblemSetVO>`.

## GET `/problem-set/{id}`

Get problem set detail, including ordered problem relations.

Response `data`: `ProblemSetVO`.

## POST `/problem-set`

Create a problem set.

Header:

| Name | Required | Description |
| --- | --- | --- |
| `X-User-Id` | yes | Current user ID |

Request body:

```json
{
  "title": "Dynamic Programming",
  "description": "Classic DP practice list",
  "status": 1,
  "problems": [
    {
      "problemId": 1,
      "sortOrder": 1,
      "note": "Start here"
    }
  ]
}
```

Response `data`: `ProblemSetVO`.

## PUT `/problem-set`

Update basic problem set information. Only the creator can update.

Request body:

```json
{
  "id": 1,
  "title": "Dynamic Programming",
  "description": "Updated description",
  "status": 1
}
```

Response `data`: `Boolean`.

## DELETE `/problem-set/{id}`

Logic-delete a problem set. Only the creator can delete.

Response `data`: `Boolean`.

## PUT `/problem-set/{id}/problems`

Replace all problem relations and persist the submitted order.

Request body:

```json
[
  {
    "problemId": 1,
    "sortOrder": 1,
    "note": "Warm-up"
  },
  {
    "problemId": 2,
    "sortOrder": 2,
    "note": "Harder variant"
  }
]
```

Response `data`: `Boolean`.

## POST `/problem-set/{id}/problems`

Append or update problem relations. Existing relations are updated with the submitted `sortOrder` and `note`.

Request body: `List<ProblemSetProblemDTO>`.

Response `data`: `Boolean`.

## DELETE `/problem-set/{id}/problems/{problemId}`

Remove one problem from a problem set.

Response `data`: `Boolean`.

## DTO Fields

`ProblemSetVO`:

| Field | Type | Description |
| --- | --- | --- |
| `id` | Long | Problem set ID |
| `title` | String | Title |
| `description` | String | Description |
| `creatorId` | Long | Creator user ID |
| `status` | Integer | `0` hidden, `1` public |
| `problemCount` | Integer | Number of linked problems |
| `problems` | List | Ordered problem relations |
| `createTime` | LocalDateTime | Create time |
| `updateTime` | LocalDateTime | Update time |
