# Hobbithon — Monorepo Overview

Polyglot microservices monorepo — a social hobby-sharing platform. Services under `apps/`, E2E tests in `tests/`, orchestrated via Docker Compose.

## Architecture

- **API Gateway** (`apps/apigateway`, container name `apigateway-service`, port 23738) — Spring Boot 4, Spring Cloud OpenFeign, GraphQL, RabbitMQ. All frontend traffic routes here first.
- **Frontend** (`apps/hobby-frontend`, port 19293) — Next.js 16 + React 19 + Tailwind CSS 4 + TanStack React Query.
- **15 microservices** in Java, Go, Elixir, Rust, C, C#. One PostgreSQL database per service (same Postgres instance).
- **RabbitMQ** (AMQP 21527, management UI 11527).
- **Redis 7** (port 6379) for the Rust library-service.
- **Monitoring**: Grafana (23333) + Prometheus (9090) + Loki (3100) + Promtail.

All containers share the `hobbithon-network` bridge network.

## Service directory

| Service | Language | Port | Key notes |
|---------|----------|------|-----------|
| `apigateway` | Java/Spring Boot 4 | 23738 | Gateway + GraphQL + OpenFeign |
| `hobby-frontend` | Next.js/TypeScript | 19293 | `NEXT_PUBLIC_*` vars baked at build time |
| `post-service` | Java/Spring Boot 3.2 | 29293 | Posts & gym tracking |
| `profile-service` | Java/Spring Boot 3.2 | 21526 | User profiles |
| `profile-connection-service` | Java/Spring Boot 3.2 | 29574 | Follows/connections |
| `interaction-service` | Java/Spring Boot 3.2 | 27087 | Likes & interactions |
| `authentication-service-c` | C (GCC) | 21525 | JWT auth, libpq, pthreads, OAuth2 |
| `notification-service` | Go 1.22 | 29555 | WebSocket hub, JWT |
| `bug-report-service` | Go 1.22 | 29556 | Bug submissions |
| `chat-service` | C# / .NET 10 | 29294 | gRPC + WebSocket chat, protobuf |
| `media_service_elx` | Elixir/Phoenix 1.8 | 27086 | Media upload/storage |
| `library-service` | Rust/Axum | 28877 | OMDB integration, sqlx migrations, Redis cache |
| `recognition-service` | Java/Spring Boot | 27088 | Image recognition |

## Database layout

Single PostgreSQL 16 instance (`db`, port 5432 local / 29496 prod). Each service has its own DB + user:

`posts_db`, `media_db`, `profile_connection_db`, `authentication_db`, `profile_db`, `interaction_db`, `library_db`, `notification_db`, `bugreport_db`, `chat_db`.

Schemas and initial tables (auth users/roles, chat tables, library extensions) are created by `init-db.sh` on first startup. The script uses `.env` variables for credentials — the prod compose passes all DB env vars directly into the `database` container.

## Docker Compose

- **Local**: `docker compose up --build` starts everything with source builds. Run `docker compose up -d database` for DB-only.
- **Prod**: `docker compose -f docker-compose-prod.yml` uses pre-built images from GitLab registry. Uses Docker Compose profiles:
  - `pgadmin` — pgAdmin web UI
  - `docs` — documentation + nginx proxy
  - `logging-monitor` — Grafana, Loki, Prometheus, Promtail
- Deploy script stops/removes frontend first, then starts core, then pgadmin, then docs, then logging-monitor in sequence.

## CI/CD (GitLab)

Stages: **validate → test → build → deploy**.

- `validate`: enforces `CHANGELOG.md` is changed in every MR targeting `main`.
- `test`: per-service jobs (Maven / `go test ./...` / `mix test` / `cargo test` / `dotnet test`), triggered on MR when service files change. Go services use `golang:1.23`, Rust uses `rust:1.96-slim` with `libssl-dev`.
- `build`: builds & pushes Docker images to GitLab registry + Docker Hub on `main` when `CHANGELOG.md` or `package.json` changes. Frontend gets `NEXT_PUBLIC_*` build args and app version injected.
- `deploy`: SSH to `student.inf.st.hsleiden.nl`, runs `docker compose -f docker-compose-prod.yml --profile logging-monitor --profile tools pull` then starts services in groups. Fails pipeline if any group fails.

## Commands & scripts

```bash
npm run changelog   # Interactive: pick change type, write entry, optionally release
npm run release     # Interactive: bump version, update CHANGELOG.md & package.json (no git commit/tag)

# E2E tests (all services must be up first)
npx playwright test                              # all tests
npx playwright test tests/posts.spec.ts          # single file
npx playwright test --headed                     # visible browser
npx playwright test --debug                      # step-through
npx playwright test -g "test name"              # by name
npx playwright show-report                      # view HTML report
```

## Testing

- **E2E**: Playwright tests in `tests/`. Browser config: chromium + firefox + webkit, 1 worker on CI, 60s timeout per test (Next.js compilation lag), 10s action timeout, 15s navigation timeout. Retries 2x on CI, 0 locally.
- Tests auto-register unique users per test via `tests/helpers/auth.ts` (timestamp + random suffix). Each test gets an isolated account.
- No root-level unit/integration tests — each service has its own test suite.
- **Service tests**: `mvn clean test` (Java), `cargo test` (Rust), `go test ./...` (Go), `mix test` (Elixir), `dotnet test` (C#).

## Commit & changelog conventions

- Conventional Commits: `feat:`, `fix:`, `chore:`, `test:`, `docs:`.
- **Always update `CHANGELOG.md`** before merging to `main` — CI validate stage enforces it.
- Use `npm run changelog` for guided entry creation.
- Use `npm run release` for version bumps (updates `CHANGELOG.md`, `package.json`, `package-lock.json`).

## Key conventions

- Service-to-service calls via OpenFeign (Java) or `net/http` (Go). URLs are read from env vars; in Docker they use `http://service-name:port`.
- JWT secret shared across services via `JWT_SECRET` and `JWT_ISSUER` env vars.
- `NEXT_PUBLIC_*` env vars are baked into the Next.js build at image-build time via Docker `--build-arg`. Not runtime-injectable.
- Each service has its own `AGENTS.md` with language/framework-specific guidance.
- Copy `.env.example` → `.env` and fill in values. `.env` is gitignored.
