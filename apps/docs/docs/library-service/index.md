---
sidebar_position: 1
---

# Library Service

The **Library Service** provides a unified searchable catalogue of books, movies, and exercises by aggregating three external APIs behind a single REST interface. Results are cached in Redis (30-day TTL) to reduce upstream API calls.

## Responsibilities

- Search books via Open Library, movies via OMDB, and exercises via ExerciseDB
- Fetch individual item details by ID (ISBN, IMDb ID, exercise ID)
- Cache API responses in Redis using a cache-aside pattern
- Allow users to create, rename, delete, and reorder personal lists of library items
- Resolve list items with full API data for display

## Ports

| Runtime | Port | Notes |
| --- | --- | --- |
| Service port | `28877` | HTTP listen port |
| Docker container port | `28877` | Exposed by `library-service` |
| Docker host port | `28877` | Mapped as `28877:28877` |

## Technology Stack

- **Language:** Rust (edition 2021)
- **Framework:** Axum 0.8 / Tokio 1.36
- **Database:** PostgreSQL (via sqlx 0.8)
- **Cache:** Redis
- **External APIs:** Open Library (books), OMDB (movies), ExerciseDB (exercises)

## Environment Variables

| Variable | Description |
| --- | --- |
| `DATABASE_URL` | PostgreSQL connection string |
| `REDIS_URL` | Redis connection URL, defaults to `redis://127.0.0.1:6379` |
| `OMDB_API_KEY` | OMDB API key (required for movie search) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated CORS origins, defaults to `http://localhost:23738` |
