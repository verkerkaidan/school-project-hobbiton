# bug-report-service

Go 1.22, plain `net/http` mux, PostgreSQL (`lib/pq`). Lightweight service for submitting and retrieving bug reports.

## Key conventions

- Same Go patterns as `notification-service` — see that service's AGENTS.md for shared idioms (DB access, env config, parameterised queries).
- No WebSocket or JWT middleware in this service — endpoints are either open or gated by a simple auth check; follow the existing pattern in handlers.
- Database name: `bugreport_db`. Port: **29556**.

## Building & testing

```bash
go build ./...
go test ./...
go run main.go
```

## Environment variables required

| Variable | Purpose |
|----------|---------|
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port |
| `DB_USER` | DB username |
| `DB_PASSWORD` | DB password |
| `DB_NAME` | `bugreport_db` |
