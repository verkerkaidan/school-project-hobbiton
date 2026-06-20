---
sidebar_position: 3
---

# Build & Deploy

## Build

A single `build` job runs on merges to `main` when `CHANGELOG.md` or `package.json` changes. It uses Docker-in-Docker to build and push images.

1. Waits for the Docker daemon to be ready.
2. Authenticates with **GitLab Container Registry** and **Docker Hub**.
3. For each service, checks if its files changed since the last commit:
   - If changed: builds the Docker image, pushes to GitLab registry, cleans up.
   - If unchanged: skips.
4. Builds **docs-proxy** separately if `docker/docs-auth/` changed.
5. Builds **hobby-frontend** with `NEXT_PUBLIC_*` build args.

### Services built

```
apigateway, authentication-service-c, chat-service, docs,
interaction-service, library-service, media_service_elx,
notification-service, bug-report-service, post-service,
profile-connection-service, profile-service, recognition-service,
hobby-frontend, docs-proxy
```

### Frontend build args

| Build arg | Source |
|-----------|--------|
| `NEXT_PUBLIC_AUTHENTICATION_URL` | CI variable |
| `NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE` | CI variable |
| `NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE` | CI variable |
| `NEXT_PUBLIC_MEDIA_URL` | CI variable |
| `NEXT_PUBLIC_INTERACTION_URL` | CI variable |
| `NEXT_PUBLIC_LIBRARY_SERVICE_URL` | CI variable |
| `NEXT_PUBLIC_NOTIFICATION_URL` | CI variable |
| `NEXT_PUBLIC_CHAT_SERVICE_URL` | CI variable |
| `NEXT_PUBLIC_WS_CHAT_SERVICE_URL` | CI variable |
| `NEXT_PUBLIC_APP_VERSION` | `jq -r '.version' package.json` |

## Deploy

The `deploy` job runs on merges to `main` (same trigger as build). It SSHes into the production server and starts services in groups.

### Server

| Setting | Value |
|---------|-------|
| Host | `student.inf.st.hsleiden.nl` |
| User | `gl_groep-01_6306` |
| Auth | SSH key (`SSH_PRIVATE_KEY` CI variable) |

### Deployment flow

1. Copies `docker-compose-prod.yml`, `init-db.sh`, and `logging-monitor/` to the server via SCP.
2. SSHes in and:
   - Authenticates with the GitLab Container Registry.
   - Exports 60+ environment variables from CI settings.
   - Pulls latest images with `docker compose --profile logging-monitor --profile tools pull`.
   - Stops and removes the frontend container.
   - Starts services in groups sequentially.

### Service groups

| Group | Command | Services |
|-------|---------|----------|
| 1. Core | `docker compose up -d --remove-orphans` | database, rabbitmq, all 15 microservices, library_redis, frontend |
| 2. pgAdmin | `--profile pgadmin up -d` | pgadmin |
| 3. Docs | `--profile docs up -d` | docs, docs-proxy |
| 4. Monitoring | `--profile logging-monitor up -d` | grafana, prometheus, loki, promtail |

If any group fails, the pipeline continues logging errors but ultimately exits with a non-zero status.

### Required CI Variables

The deploy job requires 60+ environment variables configured in GitLab CI/CD settings:

- PostgreSQL credentials for all 10 databases
- JWT secret and issuer
- OAuth credentials
- Service URLs (for inter-service discovery)
- Frontend `NEXT_PUBLIC_*` URLs
- RabbitMQ credentials
- Grafana / Loki / Prometheus configuration
- OMDB API key (library service)
- Chat service configuration (HTTP, WebSocket, DB connection string)
- Documentation proxy credentials (`DOCS_USER`, `DOCS_PASSWORD`)
- Bug report service configuration
- SSH key for deployment (`SSH_PRIVATE_KEY`)
