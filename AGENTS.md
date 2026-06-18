# Hobbithon — Monorepo Overview

This is a polyglot microservices monorepo ("BPSET P4 / Hobbithon") — a social hobby-sharing platform. All services live under `apps/`, the root `tests/` directory holds E2E tests, and `docker-compose.yml` orchestrates everything locally.

## Architecture

- **API Gateway** (`apps/apigateway`, port 23738) — Spring Boot 4, Spring Cloud OpenFeign, GraphQL, RabbitMQ. All frontend traffic routes here first.
- **Frontend** (`apps/hobby-frontend`, port 19293) — Next.js 16 + React 19 + Tailwind CSS 4 + TanStack React Query.
- **15+ microservices** written in Java, Go, Elixir, Rust, C, and C#. Each service has its own PostgreSQL database (same Postgres instance, separate DB per service).
- **Message broker**: RabbitMQ (AMQP port 21527, management UI 11527).
- **Cache**: Redis 7 (port 6379), currently used by the Rust library-service.

## Service directory

| Service | Language | Port | Key notes |
|---------|----------|------|-----------|
| `apigateway` | Java/Spring Boot 4 | 23738 | Gateway + GraphQL + OpenFeign |
| `hobby-frontend` | Next.js/TypeScript | 19293 | Frontend; env vars set at build time |
| `post-service` | Java/Spring Boot 3.2 | 29293 | Posts & gym tracking |
| `profile-service` | Java/Spring Boot 3.2 | 21526 | User profiles |
| `profile-connection-service` | Java/Spring Boot 3.2 | 29574 | Follows/connections |
| `interaction-service` | Java/Spring Boot 3.2 | 27087 | Likes & interactions |
| `authentication-service-c` | C (GCC) | 21525 | JWT auth, libpq, pthreads |
| `notification-service` | Go 1.22 | 29555 | WebSocket hub, JWT |
| `bug-report-service` | Go 1.22 | 29556 | Bug submissions |
| `chat-service` | C# / .NET 10 | 29294 | gRPC + WebSocket chat |
| `media_service_elx` | Elixir/Phoenix 1.8 | 27086 | Media upload/storage |
| `library-service` | Rust/Axum | 28877 | Currently commented out in compose |
| `recognition-service` | Java/Spring Boot | — | Currently commented out in compose |

## Database layout

Single PostgreSQL 16 instance (`db`, port 5432). Each service gets its own database:
`posts_db`, `media_db`, `profile_connection_db`, `authentication_db`, `profile_db`, `interaction_db`, `library_db`, `notification_db`, `bugreport_db`, `chat_db`.

Schemas are initialised by `init-db.sh` on first startup.

## CI/CD (GitLab)

Pipeline stages: **validate → test → build → deploy**.
- `validate`: enforces `CHANGELOG.md` is updated on every MR targeting `main`.
- `test`: per-service jobs (Maven / `go test` / `mix test`), triggered on MR when service files change.
- `build`: builds & pushes Docker images to GitLab registry + Docker Hub; runs on `main` only when `CHANGELOG.md` or `package.json` changes.
- `deploy`: SSH to `student.inf.st.hsleiden.nl`, deploys via `docker-compose-prod.yml`.

## Commit & changelog conventions

- Conventional Commits: `feat:`, `fix:`, `chore:`, `test:`, `docs:`.
- **Always update `CHANGELOG.md`** before merging to `main` — the validate stage will fail otherwise.
- Use `scripts/changelog-entry.mjs` for guidance.

## Local development

```bash
# Start everything
docker compose up --build

# Run E2E tests (all services must be up first)
npx playwright test

# Run a specific test file
npx playwright test tests/posts.spec.ts
```

Environment variables come from `.env` (not committed). Copy `.env.example` and fill in values.

## Key conventions

- Service-to-service calls use Spring OpenFeign (Java) or plain `net/http` (Go). Always read the target service's URL from an environment variable.
- JWT secret is shared across services via `JWT_SECRET` env var.
- `NEXT_PUBLIC_*` env vars are baked into the Next.js build at image-build time — they are not runtime-injectable.
- Each service has its own `AGENTS.md` with language/framework-specific guidance.
