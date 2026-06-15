---
sidebar_position: 5
---

# Deployment

## Docker Compose

The service is defined as `notification-service` in both local and production compose files.

| Setting | Value |
| --- | --- |
| Build context | `./apps/notification-service` |
| Container name | `notification-service` |
| Port mapping | `29555:29555` |
| CPU limit | `0.5` |
| Memory limit | `128m` |
| Network | `microservices-network` |
| Dependency | `database` with `service_healthy` |

## Environment Variables

| Variable | Description |
| --- | --- |
| `PORT` | HTTP listen port, defaults to `29555`. |
| `DB_HOST` | PostgreSQL hostname. In Docker this is `database`. |
| `DB_PORT` | PostgreSQL port, defaults to `5432`. |
| `DB_USER` | Notification database username. |
| `DB_PASSWORD` | Notification database password. |
| `DB_NAME` | Notification database name, defaults to `notifications`. |
| `JWT_SECRET` | JWT signing secret. |
| `JWT_ISSUER` | JWT issuer. |
| `NOTIFICATION_ALLOWED_ORIGINS` | Comma-separated allowed CORS origins. |

## Runtime Notes

- Migrations run on startup and are safe to re-run.
- WebSocket state is kept in memory, so live connections are tied to the running instance.
- The API Gateway uses `NOTIFICATION_SERVICE_URL` to call internal notification endpoints.
