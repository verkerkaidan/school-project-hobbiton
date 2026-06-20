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

| Variable | Required | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC connection string. In Docker: `jdbc:postgresql://database:5432/{db}`. |
| `SPRING_DATASOURCE_USERNAME` | Yes | Service database username. |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Service database password. |
| `DATABASE_URL` | No | Alternative libpq connection string (fallback if `SPRING_DATASOURCE_URL` not set). |
| `JWT_SECRET` | Yes | HMAC-SHA256 signing secret for JWT tokens. |
| `JWT_ISSUER` | No | JWT issuer claim, defaults to `auth-service`. |
| `OAUTH_CLIENT_ID` | No | OAuth client ID (reserved for future use). |
| `OAUTH_CLIENT_SECRET` | No | OAuth client secret (reserved for future use). |
| `ADMIN_TEST_USER` | No | Seed admin username (reserved for future use). |
| `ADMIN_TEST_PASSWORD` | No | Seed admin password (reserved for future use). |

## Build and run locally

From `apps/authentication-service-c`:

```bash
make              # build server binary (auth-service-c)
make test         # compile test binaries (test_jwt + test_db)
make valgrind     # compile with -g -O0 and run tests under Valgrind memcheck
make clean        # remove object files and binaries
```

Run tests individually:

```bash
./tests/test_jwt                          # JWT unit test (no DB required)
./tests/test_db                           # DB integration test (requires running PostgreSQL)
```

## Docker Build

Multi-stage Dockerfile:

1. **Build stage** — `debian:bookworm-slim`, installs `build-essential` and `libpq-dev`, runs `make`.
2. **Runtime stage** — `debian:bookworm-slim`, only `libpq5` and `ca-certificates`, runs the compiled binary.

## Runtime Notes

- Schema migrations run on every startup and are safe to re-run (idempotent `CREATE TABLE IF NOT EXISTS`).
- The server binary is compiled inside a multi-stage Docker build using the `Makefile`.
- The container waits for the database to be available via `netcat` before starting the service.
- Each worker thread maintains its own PostgreSQL connection (thread-local `PGconn`).
- The log file (`server.log`) is written to the working directory; if unavailable, logs go to stdout.
