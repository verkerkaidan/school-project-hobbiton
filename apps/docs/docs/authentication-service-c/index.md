---
sidebar_position: 1
---

# Authentication Service C

The **Authentication Service C** is a reimplementation of the Java authentication service in pure C. It handles user registration, login, role management, and JWT token generation without any external HTTP or crypto libraries.

## Responsibilities

- Register new users with email and password (bcrypt via PostgreSQL pgcrypto)
- Authenticate users and issue JWTs (HMAC-SHA256)
- Delete user accounts (requires authentication + password confirmation)
- Promote users to admin or staff roles (admin-only endpoints)
- Validate bearer tokens and expose user identity
- Self-contained HTTP server using POSIX sockets and pthreads
- Provides user deletion and lookup endpoints used by the gateway's registration rollback mechanism

## Ports

| Runtime | Port | Notes |
| --- | --- | --- |
| Local/default service port | `21525` | Hard-coded in `server.c`. |
| Docker container port | `21525` | Exposed by `authentication-service-c`. |
| Docker host port | `21525` | Mapped as `21525:21525`. |
| PostgreSQL port | `5432` | The service connects to the shared `database` container. |

The API Gateway reaches this service through `AUTHENTICATION_SERVICE_URL`.

## Technology Stack

- **Language:** C (C99/POSIX)
- **Database:** PostgreSQL via `libpq`
- **Crypto:** Custom SHA-256, HMAC, and base64url (no OpenSSL)
- **Password hashing:** PostgreSQL `pgcrypto` (`crypt` / `gen_salt`)
- **HTTP server:** POSIX sockets with thread pool (8 workers) and request queue
