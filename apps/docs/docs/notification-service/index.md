---
sidebar_position: 1
---

# Notification Service

The **Notification Service** stores notification records, applies user notification preferences, and pushes live updates to connected clients over WebSockets.

## Responsibilities

- Store notification records in PostgreSQL
- Push new notifications to connected clients in real time
- Track read state and unread counts per recipient
- Store per-user notification preferences
- Store opt-in subscriptions for new-post notifications from specific authors
- Support `FOLLOW`, `USER_POST`, `RECOMMENDED_POST`, `LIKE`, and `COMMENT` notification types

## Port

| Runtime | Port | Notes |
| --- | --- | --- |
| Local/default service port | `29555` | Used when `PORT` is not set. |
| Docker container port | `29555` | Exposed by `notification-service`. |
| Docker host port | `29555` | Mapped as `29555:29555`. |
| PostgreSQL port | `5432` | The service connects to the shared `database` container. |

The API Gateway reaches this service through `NOTIFICATION_SERVICE_URL`.

## Technology Stack

- **Language:** Go
- **Database:** PostgreSQL
- **Transport:** HTTP and WebSocket
