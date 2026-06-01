---
sidebar_position: 1
---

# Redis

Redis is used as an in-memory cache, primarily by the [Library Service](/docs/library-service).

## Container details

| Property       | Value            |
| -------------- | ---------------- |
| Image          | `redis:7-alpine` |
| Container name | `library_redis`  |
| Host port      | `6379`           |
| Volume         | `redis_data`     |

## Configuration

The custom configuration file is mounted at runtime:

```
./docker/redis/redis.conf → /usr/local/etc/redis/redis.conf
```

Edit `docker/redis/redis.conf` to tune memory limits, eviction policy, persistence, and other settings.

## Health check

The Docker health check runs `redis-cli ping` every 5 seconds. Services that depend on Redis (e.g. `library-service`) use `condition: service_healthy`.

## Connecting locally

```bash
# CLI via Docker
docker exec -it library_redis redis-cli

# Or directly if port 6379 is exposed
redis-cli -h localhost -p 6379
```

## Useful commands

```bash
# Monitor all commands in real-time
redis-cli monitor

# View all keys
redis-cli keys '*'

# Flush all cached data (careful in shared environments)
redis-cli flushall
```
