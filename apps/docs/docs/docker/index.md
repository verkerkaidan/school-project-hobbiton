---
sidebar_position: 1
---

# Docker

This project uses **Docker Compose** to orchestrate all services locally.

## Files

| File                      | Purpose                                             |
| ------------------------- | --------------------------------------------------- |
| `docker-compose.yml`      | Local development environment (all services + docs) |
| `docker-compose-prod.yml` | Production environment                              |

## Networks

All services share a single bridge network:

```
name: hobbithon-network
driver: bridge
```

## Volumes

| Volume          | Used by             |
| --------------- | ------------------- |
| `postgres_data` | PostgreSQL database |
| `redis_data`    | Redis cache         |

## Starting the stack

```bash
# Start all services (detached)
docker compose up -d

# Start a single service
docker compose up -d <service-name>

# View logs
docker compose logs -f <service-name>

# Stop everything
docker compose down
```

## Building images

```bash
# Rebuild a specific service after code changes
docker compose build <service-name>

# Rebuild and restart
docker compose up -d --build <service-name>
```

## Health checks

The `database` (PostgreSQL) and `redis` services expose Docker health checks. Services that depend on them use `condition: service_healthy` to ensure they start only after the dependency is ready.

## Per-service ports

| Service                          | Host port |
|----------------------------------| --------- |
| frontend                         | 19293     |
| pgadmin                          | 8000      |
| database                         | 5432      |
| post-service                     | 29293     |
| profile-connection-service       | 29574     |
| media-service-elx                | 27086     |
| recognition-service              | 27088     |
| authentication-service-c         | 21525     |
| profile-service                  | 21526     |
| library-service                  | 28877     |
| interaction-service              | 27087     |
| notification-service             | 29555     |
| bug-report-service               | 29556     |
| middleware-service (API Gateway) | 23738     |
| chat-service                     | 29294     |
| redis                            | 6379      |
| docs                             | 3001      |

## Compose services

| Service | Build context / image | Main dependencies | Key environment |
| --- | --- | --- | --- |
| `frontend` | `./apps/hobby-frontend` | API Gateway, auth, media, interaction, chat, notification URLs | `NEXT_PUBLIC_*` URLs |
| `pgadmin` | `dpage/pgadmin4:latest` | database | `.env` |
| `database` | `postgres:16` | none | `POSTGRES_USER`, `POSTGRES_PASSWORD` |
| `post-service` | `./apps/post-service` | database | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `CORS_ALLOWED_ORIGINS` |
| `profile-connection-service` | `./apps/profile-connection-service` | database | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `NOTIFICATION_SERVICE_URL`, `PROFILE_SERVICE_URL`, `CORS_ALLOWED_ORIGINS` |
| `media-service-elx` | `./apps/media_service_elx` | database | `PORT`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SECRET_KEY_BASE` |
| `recognition-service` | `./apps/recognition-service` | none | `MODEL_NAME`, `CORS_ALLOWED_ORIGINS` |
| `authentication-service-c` | `./apps/authentication-service-c` | database | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_ISSUER`, `JWT_SECRET`, OAuth/admin test values |
| `profile-service` | `./apps/profile-service` | database | `DB_HOST`, `DB_PORT`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_*`, admin test values, `CORS_ALLOWED_ORIGINS` |
| `library-service` | `./apps/library-service` | database, redis | `OMDB_API_KEY`, `OMDB_API_URL`, `REDIS_URL`, `RUST_LOG`, `CORS_ALLOWED_ORIGINS` |
| `interaction-service` | `./apps/interaction-service` | database | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `CORS_ALLOWED_ORIGINS` |
| `notification-service` | `./apps/notification-service` | database | `PORT`, `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `JWT_SECRET`, `JWT_ISSUER`, `NOTIFICATION_ALLOWED_ORIGINS` |
| `bug-report-service` | `./apps/bug-report-service` | database | `PORT`, `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `JWT_ISSUER`, `BUG_REPORT_ALLOWED_ORIGINS` |
| `middleware-service` | `./apps/apigateway` | downstream services | `*_SERVICE_URL`, `CHAT_HTTP_URL`, `CHAT_WS_URL`, `GATEWAY_ALLOWED_ORIGIN_PATTERNS`, `BUG_REPORT_SERVICE_URL` |
| `chat-service` | `./apps/chat-service` | database | `CHAT_DB_CONNECTION_STRING`, `JWT_SECRET`, `JWT_ISSUER`, `CHAT_ALLOWED_ORIGINS` |
| `docs` | `./apps/docs` | none | none |
| `redis` | `redis:7-alpine` | none | `redis.conf` mounted from `docker/redis/redis.conf` |

## Database-backed services

These services depend on PostgreSQL and should start after the `database` health check passes:

- `post-service`
- `profile-connection-service`
- `media-service-elx`
- `authentication-service-c`
- `profile-service`
- `library-service`
- `interaction-service`
- `notification-service`
- `bug-report-service`
- `chat-service`

The shared `init-db.sh` script is mounted into the PostgreSQL container as `/docker-entrypoint-initdb.d/init-db.sh`. Services that manage their own schemas use a mix of startup migrations, Hibernate schema updates, and framework migrations depending on the implementation.

## Gateway routing variables

The API Gateway container uses service URL environment variables to reach downstream services inside the Compose network:

| Variable | Target service |
| --- | --- |
| `AUTHENTICATION_SERVICE_URL` | authentication-service-c |
| `MEDIA_SERVICE_URL` | media-service-elx / media service |
| `POST_SERVICE_URL` | post-service |
| `RECOGNITION_SERVICE_URL` | recognition-service |
| `PROFILE_SERVICE_URL` | profile-service |
| `PROFILE_CONNECTION_SERVICE_URL` | profile-connection-service |
| `LIBRARY_SERVICE_URL` | library-service |
| `NOTIFICATION_SERVICE_URL` | notification-service |
| `INTERACTION_SERVICE_URL` | interaction-service |
| `CHAT_HTTP_URL` | chat-service HTTP endpoints |
| `CHAT_WS_URL` | chat-service WebSocket endpoints |
| `BUG_REPORT_SERVICE_URL` | bug-report-service |

## Resource limits

Most application services define `cpus` and `mem_limit` in Compose. Smaller Go/Rust services use lower memory limits, while JVM and frontend services use `512m`.
