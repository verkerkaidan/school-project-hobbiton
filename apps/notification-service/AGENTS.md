# notification-service

Go 1.22, plain `net/http` mux, gorilla/websocket, PostgreSQL (`lib/pq`), JWT (`golang-jwt/jwt v5`).

## Package layout

```
main.go
internal/
  auth/      — JWT parsing & validation middleware
  config/    — env-based configuration loading
  db/        — PostgreSQL connection and query functions
  handlers/  — HTTP + WebSocket request handlers
  hub/       — WebSocket broadcast hub (goroutine-based)
  models/    — shared data structs
```

## Key conventions

- **WebSocket hub**: the Hub (`internal/hub/`) owns a map of connected clients and a broadcast channel. All writes to clients go through the hub — never write to a client's connection directly from a handler.
- **Auth**: every HTTP endpoint (except the WebSocket upgrade) requires a valid JWT. The middleware in `internal/auth/` validates the token and injects the user ID into the request context. Read it with `ctx.Value(auth.UserIDKey)`.
- **Database**: connection is opened once at startup in `main.go` and passed down. Use `database/sql` idioms — always `defer rows.Close()` and check `rows.Err()` after iteration.
- **Configuration**: all config (DB DSN, JWT secret, port) comes from environment variables. Add new vars to `internal/config/` and document them in `docker-compose.yml`.
- **Concurrency**: the hub runs in its own goroutine. Communicate with it via channels, not mutexes, for new client registration and broadcast events.

## Building & testing

```bash
go build ./...
go test ./...

# Run locally (requires DB and env vars)
go run main.go
```

Port: **29555**.

## Environment variables required

| Variable | Purpose |
|----------|---------|
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port |
| `DB_USER` | DB username |
| `DB_PASSWORD` | DB password |
| `DB_NAME` | `notification_db` |
| `JWT_SECRET` | Shared JWT signing secret |
| `PORT` | HTTP listen port (default 29555) |
