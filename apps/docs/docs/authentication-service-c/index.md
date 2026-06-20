---
sidebar_position: 1
---

# Authentication Service C

The **Authentication Service C** is a reimplementation of the Java authentication service in pure C. It handles user registration, login, role management, and JWT token generation without any external HTTP or crypto libraries.

## Port

| Container port | Host port |
| -------------- | --------- |
| `21525`        | `21525`   |

The API Gateway reaches this service through `AUTHENTICATION_SERVICE_URL`.

## Responsibilities

- Register new users with email and password (bcrypt via PostgreSQL pgcrypto)
- Authenticate users and issue JWTs (HMAC-SHA256)
- Delete user accounts (requires authentication + password confirmation)
- Promote users to admin or staff roles (admin-only endpoints)
- Update email and password for authenticated users
- Validate bearer tokens and expose user identity
- Self-contained HTTP server using POSIX sockets and pthreads
- Provide user deletion and lookup endpoints used by the gateway's registration rollback mechanism
- Expose Prometheus-style metrics at `/api/auth/metrics`

## Environment Variables

| Variable | Description |
| -------- | ----------- |
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

## Technology Stack

- **Language:** C (C99/POSIX)
- **Database:** PostgreSQL via `libpq`
- **Crypto:** Custom SHA-256, HMAC, and base64url (no OpenSSL)
- **Password hashing:** PostgreSQL `pgcrypto` (`crypt` / `gen_salt`)
- **HTTP server:** POSIX sockets with thread pool (8 workers) and inline request queue
- **Build tool:** GNU Make
- **Memory checking:** Valgrind (`make valgrind`)
