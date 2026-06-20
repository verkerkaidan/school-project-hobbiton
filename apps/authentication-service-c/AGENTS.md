# authentication-service-c

C (C17), GCC, libpq (PostgreSQL client), pthreads. No framework — raw POSIX socket server.

## Source files

```
server.c       — HTTP server, request routing, entry point
crypto_core.c  — JWT signing/verification, password hashing
db_core.c      — PostgreSQL queries via libpq
test_jwt.c     — JWT unit tests
test_db.c      — DB integration tests
Makefile       — build rules
```

## Key conventions

- **Memory management**: this is C — every `malloc` must have a corresponding `free`. Use Valgrind locally when adding new code paths to check for leaks.
- **Error handling**: check the return value of every system call and libpq function. Log errors to `stderr`. Never silently swallow a `NULL` return from `PQexec`.
- **libpq**: use parameterised queries (`PQexecParams`) exclusively — never string-interpolate user input into SQL. Always call `PQclear(result)` after processing a result set.
- **JWT**: signing and verification are in `crypto_core.c`. The shared `JWT_SECRET` env var is read at startup. Do not duplicate JWT logic outside `crypto_core.c`.
- **Threading**: the server uses pthreads for concurrent request handling. Protect shared state with a mutex; keep critical sections short.
- **Build**: always run `make clean && make` after editing headers to avoid stale object files.

## Building & testing

```bash
make            # build server binary
make test       # compile (test_jwt + test_db)
make valgrind   # compile with -g -O0 and run tests under Valgrind memcheck
make clean      # remove object files and binaries
```

Port: **21525**.

## Environment variables required

| Variable | Purpose |
|----------|---------|
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port |
| `DB_USER` | DB username |
| `DB_PASSWORD` | DB password |
| `DB_NAME` | `authentication_db` |
| `JWT_SECRET` | JWT signing secret |

## Important

This service is the auth source of truth — it issues JWTs consumed by all other services. Any change to the JWT payload structure or signing algorithm must be coordinated across the entire platform.
