---
sidebar_position: 1
---

# Library Service

The **Library Service** provides a searchable catalogue of movies, books, and other media using the OMDB API. Results are cached in Redis to reduce upstream API calls.

## Port

| Container port | Host port |
| -------------- | --------- |
| `28877`        | `28877`   |

## Responsibilities

- Query the OMDB API for media metadata
- Cache responses in Redis
- Serve library search endpoints to the frontend

## Environment Variables

| Variable       | Description                                      |
| -------------- | ------------------------------------------------ |
| `OMDB_API_KEY` | OMDB API key                                     |
| `OMDB_API_URL` | Base URL for the OMDB API                        |
| `REDIS_URL`    | Redis connection URL (e.g. `redis://redis:6379`) |

## Caching

Redis is used as a read-through cache. Cache entries expire automatically. See the [Redis docs](/docs/redis) for configuration details.

## Technology Stack

- **Language:** Rust
- **Framework:** Axum / Tokio
- **Build tool:** Cargo
- **Cache:** Redis
