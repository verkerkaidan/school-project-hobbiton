# Notification Service

A lightweight real-time notification service written in Go, using WebSockets for live delivery and PostgreSQL for persistence.

## Features

- Real-time push notifications over WebSocket (`GET /ws`)
- Per-user notification preferences (opt-in/opt-out per type)
- Post subscription system — users can subscribe to specific authors' posts
- Batch notification creation for fan-out (internal endpoint, not exposed publicly)
- JWT authentication on all user-facing endpoints
- CORS support with configurable allowed origins

## Tech Stack

- **Language:** Go 1.22
- **WebSockets:** `gorilla/websocket`
- **Auth:** `golang-jwt/jwt` v5
- **Database:** PostgreSQL (`lib/pq`)
- **Default port:** `29555`

## API Endpoints

| Method  | Path                                  | Auth                        | Description                                      |
| ------- | ------------------------------------- | --------------------------- | ------------------------------------------------ |
| `GET`   | `/health`                             | None                        | Health check                                     |
| `GET`   | `/ws`                                 | JWT (query param `?token=`) | WebSocket connection for real-time notifications |
| `POST`  | `/notifications`                      | JWT                         | Create a notification                            |
| `POST`  | `/notifications/batch`                | None (internal)             | Fan-out notifications to multiple recipients     |
| `GET`   | `/notifications`                      | JWT                         | List notifications for the current user          |
| `PATCH` | `/notifications/{id}/read`            | JWT                         | Mark a notification as read                      |
| `POST`  | `/notifications/read-all`             | JWT                         | Mark all notifications as read                   |
| `GET`   | `/notifications/unread-count`         | JWT                         | Get unread notification count                    |
| `GET`   | `/notification-preferences`           | JWT                         | Get notification preferences                     |
| `PUT`   | `/notification-preferences/{type}`    | JWT                         | Update preference for a notification type        |
| `GET`   | `/user-post-subscriptions/{authorId}` | JWT                         | Check subscription to an author                  |
| `PUT`   | `/user-post-subscriptions/{authorId}` | JWT                         | Toggle subscription to an author's posts         |

> `/notifications/batch` is an internal endpoint and must not be exposed through the API gateway.

## Environment Variables

| Variable                       | Default                                         | Description                                                       |
| ------------------------------ | ----------------------------------------------- | ----------------------------------------------------------------- |
| `PORT`                         | `29555`                                         | HTTP server port                                                  |
| `DB_HOST`                      | `localhost`                                     | PostgreSQL host                                                   |
| `DB_PORT`                      | `5432`                                          | PostgreSQL port                                                   |
| `DB_USER`                      |                                                 | Database username                                                 |
| `DB_PASSWORD`                  |                                                 | Database password                                                 |
| `DB_NAME`                      | `notifications`                                 | Database name                                                     |
| `JWT_SECRET`                   |                                                 | Secret for validating JWT tokens                                  |
| `JWT_ISSUER`                   |                                                 | Expected JWT issuer claim                                         |
| `NOTIFICATION_ALLOWED_ORIGINS` | `http://localhost:19293,http://localhost:23738` | Comma-separated list of allowed CORS origins (exact origins only) |

## Running Locally

```bash
go run .
```

## Running Tests

```bash
go test ./...
```
