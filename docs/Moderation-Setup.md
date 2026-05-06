# Blog Text Moderation Setup

## Runtime dependencies

The moderation flow uses RabbitMQ and the new `moderation-service`.

Docker Compose now starts:

- `rabbitmq` on `5672`, management UI on `15672`
- `moderation-service` on `9060`

## Aliyun credentials

The Aliyun SDK reads credentials from the default Credentials chain. Configure these environment variables before starting `moderation-service`:

```powershell
$env:ALIBABA_CLOUD_ACCESS_KEY_ID="your-access-key-id"
$env:ALIBABA_CLOUD_ACCESS_KEY_SECRET="your-access-key-secret"
```

For Docker Compose, put them in a local `.env` file that is not committed:

```env
ALIBABA_CLOUD_ACCESS_KEY_ID=your-access-key-id
ALIBABA_CLOUD_ACCESS_KEY_SECRET=your-access-key-secret
MODERATION_INTERNAL_TOKEN=change-this-in-production
```

## Existing database migration

Fresh databases use the updated `sql/emiya_oj_blog.sql`.

For an existing `emiya_oj_blog` database, run:

```sql
source sql/emiya_oj_blog_moderation_migration.sql;
```

Existing blogs and comments are initialized as `APPROVED`.

## Audit states

`blog.audit_status` and `blog_comment.audit_status` use:

| Value | Status |
| --- | --- |
| `0` | `PENDING` |
| `1` | `APPROVED` |
| `2` | `REJECTED` |
| `3` | `MANUAL_REVIEW` |

New or edited text starts as `PENDING`, is hidden from public queries, and becomes public only when Aliyun `ScanText` returns `pass`.

## Labels

The moderation service sends these labels to `ScanText`:

`spam`, `politics`, `abuse`, `terrorism`, `porn`, `flood`, `contraband`, `ad`.

`block` becomes `REJECTED`; `review`, empty results, SDK exceptions, or callback failures become `MANUAL_REVIEW`.
