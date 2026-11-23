# School Project: Hobbiton

Hobbiton is a social platform for sharing hobbies: users post, follow each other, chat, get notifications, and keep a library of things they like. It was built by a team of five as a school project (Software Engineering 2, Hogeschool Leiden) and is deliberately a **polyglot microservices system**: each service is written in a different language and has its own database.

This repository is a copy of the team's original monorepo, including its full history. Some internal names (compose files, `AGENTS.md`, container names) still say "Hobbithon", the project's working name.

## My contribution

I was responsible for:

- **The frontend** (`apps/hobby-frontend`): a Next.js 16 / React 19 app with Tailwind CSS 4 and TanStack React Query. It covers the feed, posts, discover, profiles, chats, notifications, settings and the admin area.
- **The Go services**:
  - `apps/notification-service`: notifications, with a goroutine-based WebSocket hub for real-time delivery, JWT auth, and per-user notification preferences.
  - `apps/bug-report-service`: lightweight service for submitting and retrieving bug reports.
- **The Profile Connections Service** (`apps/profile-connection-service`, Java / Spring Boot): follows, blocks, and user reports, plus an audit log and moderation workflow for reports.

The other services were built by my teammates.

## Architecture

All frontend traffic goes through the API gateway, which routes to the services. Every service has its own PostgreSQL database on a shared Postgres instance.

| Service | Language | Port | Purpose |
|---|---|---|---|
| `hobby-frontend` | Next.js / TypeScript | 19293 | Web UI |
| `apigateway` | Java / Spring Boot 4 | 23738 | Gateway, GraphQL, OpenFeign |
| `authentication-service-c` | C | 21525 | JWT auth |
| `profile-service` | Java / Spring Boot | 21526 | Profiles |
| `profile-connection-service` | Java / Spring Boot | 29574 | Follows, blocks, reports |
| `post-service` | Java / Spring Boot | 29293 | Posts |
| `interaction-service` | Java / Spring Boot | 27087 | Likes and interactions |
| `recognition-service` | Java / Spring Boot | 27088 | Image recognition |
| `notification-service` | Go | 29555 | Real-time notifications |
| `bug-report-service` | Go | 29556 | Bug reports |
| `chat-service` | C# / .NET | 29294 | gRPC and WebSocket chat |
| `media_service_elx` | Elixir / Phoenix | 27086 | Media upload and storage |
| `library-service` | Rust / Axum | 28877 | OMDb-backed library, Redis cache |

Supporting infrastructure: RabbitMQ, Redis, Grafana, Prometheus, Loki and Promtail (monitoring), plus a Docusaurus docs site (`apps/docs`).

## Other contents

- `tests/`: Playwright end-to-end tests that cover the main user flows.
- `docker-compose.yml` and `docker-compose-prod.yml`: local and production setups.
- `init-db.sh`: creates the per-service databases on first start.
- `.gitlab-ci.yml` and `.gitlab/`: the original GitLab CI pipeline. It does not run on GitHub.
- `CHANGELOG.md`: release notes, kept in Keep a Changelog format.

## How to run

**Requirements:** Docker with Docker Compose. For the E2E tests you also need Node.js 20+. For the frontend alone you need Node.js 20+.

### Full stack with Docker

```bash
git clone https://github.com/verkerkaidan/school-project-hobbiton.git
cd school-project-hobbiton
cp .env.example .env
```

Fill in `.env` with your own values (database credentials, `JWT_SECRET`, `JWT_ISSUER`, an admin test account, and an [OMDb API key](https://www.omdbapi.com/apikey.aspx) for the library service). Then start everything:

```bash
docker compose up --build
```

The first build takes a while because every service is built from source. The frontend is available at http://localhost:19293 and the gateway at http://localhost:23738.

To start only the database while developing a single service:

```bash
docker compose up -d database
```

### Frontend only

```bash
cd apps/hobby-frontend
npm install
npm run dev
```

Set the `NEXT_PUBLIC_*` variables from `.env.example` so it can reach the gateway. In Docker these are baked in at build time.

### Go services

```bash
cd apps/notification-service   # or apps/bug-report-service
go test ./...
go run main.go                 # needs a database and the env vars listed in the service's AGENTS.md
```

### Profile Connections Service

```bash
cd apps/profile-connection-service
./mvnw clean test              # or: mvn clean test
```

### End-to-end tests

With the full stack running:

```bash
npm install
npx playwright install
npx playwright test
```

## Team

Built by Aidan Verkerk, Dagmar Dinjens, Demi Elstgeest, Matthijs Harteveld and Tygo Houweling.
