# Redis Cache Setup

The backend uses Spring Cache for dashboard, money plan, and category reads.

## Local Development

Start Redis:

```bash
docker compose up -d redis
```

Local configuration defaults to:

```text
REDIS_URL=redis://localhost:6379
```

## Render Production

Production works without Redis Cloud by using in-memory caching:

```text
CACHE_TYPE=simple
```

When cloud Redis is ready, set:

```text
CACHE_TYPE=redis
REDIS_URL=redis://default:<password>@<host>:<port>
```

Then redeploy the backend.

## Cache Expiry

| Cache | TTL |
| --- | --- |
| Dashboard | 5 minutes |
| Money plan | 5 minutes |
| Categories | 30 minutes |

Income, expense, budget, savings, recurring transaction, bill reminder, and category changes clear related caches.
