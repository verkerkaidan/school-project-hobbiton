---
sidebar_position: 1
---

# Media Service (Elixir)

The **Media Service (Elixir)** is an alternative implementation of the media service built with the Phoenix framework.

## Port

| Container port | Host port |
| -------------- | --------- |
| `27089`        | `27089`   |

## Responsibilities

- Accept multipart file uploads
- Store and serve media assets
- Leverage Elixir concurrency for high throughput

## Environment Variables

| Variable          | Description             |
| ----------------- | ----------------------- |
| `DB_HOST`         | PostgreSQL hostname     |
| `DB_PORT`         | PostgreSQL port         |
| `DB_NAME`         | Database name           |
| `DB_USERNAME`     | DB username             |
| `DB_PASSWORD`     | DB password             |
| `SECRET_KEY_BASE` | Phoenix secret key base |

## Technology Stack

- **Language:** Elixir
- **Framework:** Phoenix
- **Build tool:** Mix
- **Database:** PostgreSQL
