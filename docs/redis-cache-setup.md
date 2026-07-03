# Redis Cache Setup

This backend now uses Spring Cache with Redis.

## What Redis Caches

| Cache | Data | TTL |
| --- | --- | --- |
| `dashboard` | Dashboard totals and recent transactions | 5 minutes |
| `moneyPlan` | Money plan summary, budget alerts, cashflow forecast | 5 minutes |
| `categories` | User categories and category dropdown data | 30 minutes |

## Local Development

Run Redis with Docker:

```bash
docker compose up -d redis
```

Local Spring Boot uses:

```text
REDIS_URL=redis://localhost:6379
```

That is already the default in `application.properties`.

## Production

Use a cloud Redis provider such as Redis Cloud, Upstash, or Render Redis.

In Render backend environment variables, add:

```text
REDIS_URL=redis://default:<password>@<host>:<port>
```

Then redeploy the backend.

## Cache Invalidation

The backend clears cached dashboard and money plan data when money data changes:

- Add/delete income
- Add/delete expense
- Add/edit/delete budget
- Add/edit/delete savings goals
- Add savings contribution
- Add/edit/delete recurring transactions
- Process due recurring transactions
- Add/edit/delete bill reminders

The backend clears category cache when categories change.

This keeps pages fast while still showing fresh data after user changes.
