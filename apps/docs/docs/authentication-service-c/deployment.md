---
sidebar_position: 5
---

# Deployment

## Docker Compose

The service is defined as `authentication-service-c` in the local compose file.

| Setting | Value |
| --- | --- |
| Build context | `./apps/authentication-service-c` |
| Container name | `authentication-service-c` |
| Port mapping | `21525:21525` |
| CPU limit | `0.5` |
| Memory limit | `512m` |
| Network | `microservices-network` |
| Dependency | `database` with `service_healthy` |

## Environment Variables

| Variable | Description |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection string. In Docker: `jdbc:postgresql://database:5432/{db}`. |
| `SPRING_DATASOURCE_USERNAME` | Service database username. |
| `SPRING_DATASOURCE_PASSWORD` | Service database password. |
| `DATABASE_URL` | Alternative libpq connection string (fallback if `SPRING_DATASOURCE_URL` not set). |
| `JWT_SECRET` | HMAC-SHA256 signing secret for JWT tokens. |
| `JWT_ISSUER` | JWT issuer claim, defaults to `auth-service`. |
| `OAUTH_CLIENT_ID` | OAuth client ID (reserved for future use). |
| `OAUTH_CLIENT_SECRET` | OAuth client secret (reserved for future use). |
| `ADMIN_TEST_USER` | Seed admin username (reserved for future use). |
| `ADMIN_TEST_PASSWORD` | Seed admin password (reserved for future use). |

## Runtime Notes

- Schema migrations run on every startup and are safe to re-run (idempotent `CREATE TABLE IF NOT EXISTS`).
- The server binary is compiled inside a multi-stage Docker build using the `Makefile`.
- The container waits for the database to be available via `netcat` before starting the service.
- Each worker thread maintains its own PostgreSQL connection.
- The log file (`server.log`) is written to the working directory; if unavailable, logs go to stdout.
