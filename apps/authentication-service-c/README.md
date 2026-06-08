Authentication Service (C)

This is a reimplementation of the Java `authentication-service` in C that integrates with the original PostgreSQL database and uses BCrypt for password hashing.

Features
- HTTP server (single binary) using POSIX sockets and pthreads
- Endpoints (same paths as Java):
  - POST /auth/register  -> {"email":"","password":""}
  - POST /auth/login     -> {"email":"","password":""}
  - DELETE /auth/delete  -> Authorization: Bearer <jwt> and body {"email":"","password":""}
  - PUT /auth/admin/new/<userId> -> Authorization: Bearer <admin-jwt>
  - PUT /auth/staff/new/<userId> -> Authorization: Bearer <admin-jwt>
  - GET /Oauth/dashboard -> Authorization: Bearer <jwt>
  - GET /Oauth/me        -> Authorization: Bearer <jwt>
- JWT (HMAC-SHA256), claims: subject="User Details", userId, role (array), iat, exp
- Password hashing: BCrypt (via system crypt()/crypt_r with bcrypt salt). The implementation generates proper bcrypt salts and uses crypt()/crypt_r for hashing/verification.
- Storage: connects to the same PostgreSQL database used by the Java service (reads/writes `custom_user`, `role`, `users_roles` tables)
- Unit tests for bcrypt, JWT, and DB integration (tests require a running Postgres and proper environment variables)

Build
- Requires libpq (PostgreSQL client library) and a libc that provides crypt/crypt_r with bcrypt support (libxcrypt or similar).
- No OpenSSL dependency.

Makefile targets
- make         -> build `auth-service-c`
- make tests   -> build tests (and `make test` runs them sequentially)
- make clean   -> remove binaries

Run
- ./auth-service
- The service listens on port 8080.

Environment variables (for DB and JWT)
- SPRING_DATASOURCE_URL (e.g. jdbc:postgresql://localhost:5432/yourdb)
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- or you may use standard PG env vars: PGHOST, PGPORT, PGDATABASE
- JWT_SECRET (default: "your-secret")
- JWT_ISSUER (default: "auth-service")
- ADMIN_USER / ADMIN_PASSWORD (defaults: admin@localhost / AdminPass123!)

Tests
- `make test` builds tests and runs them. `tests/test_db` requires a reachable Postgres database (set SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD).

Notes and limitations
- The C service relies on the presence of the same schema that the Java service expects: tables `custom_user`, `role`, and join table `users_roles` as configured in the Java JPA entities. The C code assumes column names used by the JPA entities (for example `custom_user.password` contains the bcrypt hash string).
- BCrypt is performed using the system crypt()/crypt_r call. This requires a libc/crypt implementation that supports bcrypt (common on modern Linux with libxcrypt). If your platform doesn't support bcrypt in crypt, let me know and I can vendor a portable bcrypt implementation.
- The JWT code is implemented in pure C (no OpenSSL). It uses an internal SHA256+HMAC implementation.
- The HTTP server is intentionally minimal and not hardened for production.
