---
sidebar_position: 5
---

# Deployment

## Docker Compose

The service is defined as `library-service` in both local and production compose files.

| Setting | Value |
| --- | --- |
| Build context | `./apps/library-service` |
| Container name | `library-service` |
| Port mapping | `28877:28877` |
| Network | `microservices-network` |
| Dependencies | `redis` (healthy), `database` (healthy) |

## Environment Variables

| Variable | Description |
| --- | --- |
| `DATABASE_URL` | PostgreSQL connection string (e.g. `postgres://user:pass@database:5432/bpset_library`) |
| `REDIS_URL` | Redis connection URL, defaults to `redis://127.0.0.1:6379`. In Docker: `redis://redis:6379` |
| `OMDB_API_KEY` | OMDB API key (required for movie search) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated CORS origins, defaults to `http://localhost:23738` |

## Local Development

Requires a running Redis instance. Start PostgreSQL and Redis, then:

```bash
cd apps/library-service
cargo run
```

The service will retry connecting to Redis and PostgreSQL until they are available.

## Docker Compose (recommended)

```bash
docker compose up --build
```

Redis data is persisted in a named Docker volume (`redis_data`).

## Runtime Notes

- Retry loops: the service retries Redis every 2 seconds and PostgreSQL every 5 seconds on startup until they become available.
- Migrations run automatically on startup via sqlx (`./migrations/001_lists.sql`).
- Cache failures are non-fatal — if Redis becomes unavailable, the service logs a warning and disables caching for 30 seconds, then re-enables.
- The API Gateway routes requests to `/api/library/*` using `LIBRARY_SERVICE_URL`.
