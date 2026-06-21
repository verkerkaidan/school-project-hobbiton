---
sidebar_position: 2
---

# Notification Service Architecture

## Startup Flow

1. Load configuration from environment variables.
2. Connect to PostgreSQL.
3. Run idempotent migrations for notification tables, preferences, and post subscriptions.
4. Create the in-memory WebSocket hub.
5. Register HTTP routes and start listening on `PORT`, defaulting to `29555`.

## Core Components

- `main.go`
  - loads config, connects to the database, runs migrations, starts the WebSocket hub, and serves HTTP.
- `internal/handlers`
  - exposes health, notification CRUD, preferences, post subscriptions, and WebSocket upgrade endpoints.
- `internal/hub`
  - tracks active WebSocket clients by user and forwards live notification payloads.
- `internal/db`
  - connects to PostgreSQL and creates required tables/indexes.
- `internal/auth`
  - validates JWTs used by user-facing HTTP endpoints and WebSocket connections.
- `internal/metrics`
  - registers Prometheus counters and histograms, wraps every handler to record request count and duration, and exposes a `/metrics` endpoint.

## General Flow

1. Clients open `GET /ws?token=<jwt>` to register a WebSocket connection for the authenticated user.
2. Internal callers create notifications through `POST /notifications` or `POST /notifications/batch`.
3. Before persisting, the service checks the recipient's notification preference for that type.
4. For batch `USER_POST` notifications, the service also checks whether each recipient is subscribed to the actor/author.
5. Accepted notifications are inserted into PostgreSQL and pushed to any active WebSocket connection for the recipient.
6. Authenticated users can list notifications, mark one or all as read, and fetch unread counts.

## Sequence Diagrams

### Single Notification Creation

```mermaid
sequenceDiagram
  participant Caller as Gateway or service
  participant Notification as Notification Service
  participant DB as PostgreSQL
  participant Hub as WebSocket Hub
  participant Client as Connected client

  Caller->>Notification: POST /notifications
  Notification->>DB: Read recipient preference
  alt Preference disabled
    Notification-->>Caller: 200 skipped
  else Preference enabled or unset
    Notification->>DB: Insert notifications row
    Notification->>Hub: Send NOTIFICATION envelope
    Hub-->>Client: WebSocket message
    Notification-->>Caller: 201 notification
  end
```

### User Post Batch Notification

```mermaid
sequenceDiagram
  participant Caller as Post flow caller
  participant Notification as Notification Service
  participant DB as PostgreSQL
  participant Hub as WebSocket Hub

  Caller->>Notification: POST /notifications/batch
  loop Each recipientId
    Notification->>DB: Check USER_POST preference
    Notification->>DB: Check user_post_subscriptions row
    alt Enabled and subscribed
      Notification->>DB: Insert notification
      Notification->>Hub: Push to recipient if connected
    else Disabled or not subscribed
      Notification-->>Notification: Increment skipped count
    end
  end
  Notification-->>Caller: 200 { created, skipped }
```

### Self-Service Notification Deletion

```mermaid
sequenceDiagram
  participant Client as Authenticated user
  participant Notification as Notification Service
  participant DB as PostgreSQL

  Client->>Notification: DELETE /notifications/users/me
  Notification->>Notification: Validate Bearer JWT
  alt Invalid token
    Notification-->>Client: 401 Unauthorized
  else Valid token
    Notification->>DB: DELETE WHERE user_id = me OR actor_id = me
    Notification-->>Client: 200 { "deleted": N }
  end
```

### Admin Notification Deletion

```mermaid
sequenceDiagram
  participant Admin as Admin caller
  participant Notification as Notification Service
  participant DB as PostgreSQL

  Admin->>Notification: DELETE /notifications/users/{userId}
  Notification->>Notification: Validate Bearer JWT
  alt Invalid token
    Notification-->>Admin: 401 Unauthorized
  else Valid token, missing ROLE_ADMIN
    Notification-->>Admin: 403 Forbidden
  else Valid admin token
    Notification->>DB: DELETE WHERE user_id = userId OR actor_id = userId
    Notification-->>Admin: 200 { "deleted": N }
  end
```

### WebSocket Registration

```mermaid
sequenceDiagram
  participant Client as Browser
  participant Notification as Notification Service
  participant Hub as WebSocket Hub

  Client->>Notification: GET /ws?token=<jwt>
  Notification->>Notification: Validate JWT
  Notification->>Hub: Register user connection
  loop Keepalive
    Notification-->>Client: Ping
    Client-->>Notification: Pong
  end
  Notification->>Hub: Unregister on disconnect
```
