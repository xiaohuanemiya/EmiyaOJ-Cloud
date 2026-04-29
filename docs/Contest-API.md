# Contest API

All endpoints return `ResponseResult<T>`:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## Rules

`ruleType`:

| Value | Rule |
| --- | --- |
| `1` | ACM/ICPC |
| `2` | IOI |
| `3` | Codeforces |

`status`: `0` draft, `1` published, `2` cancelled.

Invite codes are exactly 10 characters. Allowed characters are `A-Z`, `a-z`, `0-9`, and `! @ # $ % & * _ - + = ?`. If omitted when creating a contest, the service generates one with `SecureRandom`.

Contest admins can modify contest problems. The creator is always kept as an admin. Additional admins must be users who have the RBAC permission code `CONTEST`.

## GET `/contest/list`

Query contests.

Query:

| Name | Required | Description |
| --- | --- | --- |
| `pageNum` | no | Page number |
| `pageSize` | no | Page size |
| `title` | no | Fuzzy title filter |
| `ruleType` | no | `1`, `2`, or `3` |
| `status` | no | `0`, `1`, or `2` |
| `startFrom` | no | Start time lower bound |
| `startTo` | no | Start time upper bound |

Response `data`: `PageVO<ContestVO>`.

## GET `/contest/{id}`

Get contest detail. Non-admin users do not receive `inviteCode`.

Response `data`: `ContestVO`.

## POST `/contest`

Create a contest. The creator is inserted into `contest_admin` automatically.

Request body:

```json
{
  "title": "Weekly Contest 1",
  "description": "Four-hour contest",
  "ruleType": 1,
  "startTime": "2026-05-01T14:00:00",
  "endTime": "2026-05-01T18:00:00",
  "freezeBeforeMinutes": 60,
  "inviteCode": "Ab3!Cd4?Ef",
  "status": 1,
  "problems": [
    {
      "problemId": 1,
      "label": "A",
      "sortOrder": 1,
      "score": 100
    }
  ]
}
```

Response `data`: `ContestVO`.

## PUT `/contest`

Update contest metadata. Only contest admins can update.

Response `data`: `Boolean`.

## DELETE `/contest/{id}`

Logic-delete a contest. Only contest admins can delete.

Response `data`: `Boolean`.

## POST `/contest/{id}/register`

Register for a contest with the invite code.

Request body:

```json
{
  "inviteCode": "Ab3!Cd4?Ef"
}
```

Registration is allowed after publish and before contest end. Duplicate registration is treated as success.

Response `data`: `Boolean`.

## DELETE `/contest/{id}/register`

Cancel current user registration. Cancellation is rejected after the contest starts.

Response `data`: `Boolean`.

## GET `/contest/{id}/registrations`

List registrations. Contest admins only.

Response `data`: `List<ContestRegistrationVO>`.

## DELETE `/contest/{id}/registrations/{userId}`

Remove a registered user. Contest admins only.

Response `data`: `Boolean`.

## PUT `/contest/{id}/problems`

Replace all contest problems. Contest admins only.

Request body:

```json
[
  {
    "problemId": 1,
    "label": "A",
    "sortOrder": 1,
    "score": 100
  },
  {
    "problemId": 2,
    "label": "B",
    "sortOrder": 2,
    "score": 200
  }
]
```

Response `data`: `Boolean`.

## GET `/contest/admin-candidates`

Return users who have RBAC permission code `CONTEST`.

Response `data`: `List<UserVO>`.

## PUT `/contest/{id}/admins`

Replace contest admins. The creator is retained even if omitted.

Request body:

```json
{
  "userIds": [1001, 1002]
}
```

Response `data`: `Boolean`.

## GET `/contest/{id}/rank`

Get contest ranking.

Freeze behavior: while `now >= endTime - freezeBeforeMinutes` and `now < endTime`, non-admin users only see submissions created before the freeze time. Admins always see complete ranking. When the contest ends, rankings are automatically unsealed.

Response `data`: `ContestRankVO`.

## GET `/contest/internal/{id}/submit-check`

Internal endpoint used by Judge service before contest submissions are accepted.

Query:

| Name | Required | Description |
| --- | --- | --- |
| `problemId` | yes | Problem ID |
| `userId` | yes | Submitter user ID |

Response `data`:

```json
{
  "allowed": true,
  "contestProblemId": 1,
  "message": "OK"
}
```

## Judge Submit Relationship

`SubmitCodeDTO` accepts optional `contestId`:

```json
{
  "contestId": 1,
  "problemId": 1,
  "languageId": 1,
  "code": "..."
}
```

When `contestId` is present, Judge calls `/contest/internal/{id}/submit-check`. The submission is rejected if the contest is unpublished, not active, ended, the user is not registered, or the problem is not part of the contest.

`SubmissionVO` includes `contestId` and `contestProblemId`.
