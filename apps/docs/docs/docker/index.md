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
| -------------------------------- | --------- |
| frontend                         | 19293     |
| pgadmin                          | 8000      |
| database                         | 5432      |
| post-service                     | 29293     |
| profile-connection-service       | 29574     |
| media-service                    | 27086     |
| media-service-elx                | 27089     |
| recognition-service              | 27088     |
| authentication-service           | 21525     |
| profile-service                  | 21526     |
| library-service                  | 28877     |
| interaction-service              | 27087     |
| notification-service             | 29555     |
| middleware-service (API Gateway) | 23738     |
| chat-service                     | 29294     |
| redis                            | 6379      |
| docs                             | 3001      |
