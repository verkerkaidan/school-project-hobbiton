---
sidebar_position: 5
---

# Deployment

## Docker Compose

The service is defined as `bug-report-service` in both local and production compose files.

| Setting | Value |
| --- | --- |
| Build context | `./apps/bug-report-service` |
| Container name | `bug-report-service` |
| Port mapping | `29556:29556` |
| CPU limit | `0.5` |
| Memory limit | `128m` |
| Network | `microservices-network` |
| Dependency | `database` with `service_healthy` |

## Environment Variables

| Variable | Description |
| --- | --- |
| `PORT` | HTTP listen port, defaults to `29556`. |
| `DB_HOST` | PostgreSQL hostname. In Docker this is `database`. |
| `DB_PORT` | PostgreSQL port, defaults to `5432`. |
| `DB_USER` | Service database username. |
| `DB_PASSWORD` | Service database password. |
| `DB_NAME` | Service database name, defaults to `bug_reports`. |
| `POSTGRES_USER` | Optional admin user used to bootstrap the service database/user. |
| `POSTGRES_PASSWORD` | Optional admin password used to bootstrap the service database/user. |
| `JWT_SECRET` | JWT signing secret used for bearer-token validation. |
| `JWT_ISSUER` | JWT issuer. |
| `BUG_REPORT_ALLOWED_ORIGINS` | Comma-separated CORS origin regex patterns, defaults to `*`. |

## Runtime Notes

- Migrations run on startup and are safe to re-run.
- If admin PostgreSQL credentials are not provided, the service assumes its database and user already exist.
- The API Gateway uses `BUG_REPORT_SERVICE_URL` to route user and admin bug report actions.
