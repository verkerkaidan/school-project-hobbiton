---
sidebar_position: 1
---

# Notification Service

The **Notification Service** delivers real-time push notifications to connected clients over WebSockets / SSE.

## Port

| Container port | Host port |
| -------------- | --------- |
| `29555`        | `29555`   |

## Responsibilities

- Store notification records in PostgreSQL
- Push new notifications to connected clients in real-time
- Support multiple notification types (follow, like, comment, etc.)

## Environment Variables

| Variable                       | Description                          |
| ------------------------------ | ------------------------------------ |
| `PORT`                         | HTTP listen port                     |
| `DB_HOST`                      | PostgreSQL hostname                  |
| `DB_PORT`                      | PostgreSQL port                      |
| `DB_USER`                      | DB username                          |
| `DB_PASSWORD`                  | DB password                          |
| `DB_NAME`                      | Database name                        |
| `JWT_SECRET`                   | JWT signing secret                   |
| `JWT_ISSUER`                   | JWT issuer                           |
| `NOTIFICATION_ALLOWED_ORIGINS` | Comma-separated allowed CORS origins |

## Technology Stack

- **Language:** Go
- **Database:** PostgreSQL
