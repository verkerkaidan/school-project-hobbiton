# Chat Service

The **Chat Service** is a .NET 10 microservice that handles real-time chat delivery, chat membership, and unread notification state.

## Overview

The service serves as the chat backend for Hobbithon. It provides:

- persistent chat rooms and messages
- WebSocket-based real-time chat delivery
- unread count notifications over a dedicated WebSocket channel
- chat membership and title management

## Quick links

- [Architecture](#architecture)
- [HTTP API](#http-api)
- [WebSocket API](#websocket-api)
- [Database Schema](#database-schema)
- [Deployment & Configuration](#deployment)

## Port

| Container port | Host port |
| -------------- | --------- |
| `29294`        | `29294`   |

## Responsibilities

- Persist and retrieve chat messages
- WebSocket real-time delivery
- Chat room metadata and membership management
- Unread message notification updates

## Configuration

The service requires `CHAT_DB_CONNECTION_STRING` to connect to PostgreSQL. The database connection string should be injected via environment variables.

## Technology Stack

- **Language:** C#
- **Framework:** ASP.NET Core (.NET 10)
- **Build tool:** `dotnet` / MSBuild
- **Database:** PostgreSQL
- **Packages:** `Npgsql`, `Microsoft.AspNetCore.OpenApi`, `Google.Protobuf`, `Grpc.Tools`

# HTTP-API

The chat-service exposes REST endpoints for chat metadata and membership management.

## Endpoints

| Method | Path | Body | Description |
| --- | --- | --- | --- |
| POST | `/create-chat` | `{ "userIds": ["<userId>"], "title": "Group Chat" }` | Create a chat and grant membership to the provided users. Returns `{ "chatId": "..." }`. |
| GET | `/get-chats/{userId}` | none | List all chats accessible by the user. Returns chat IDs and titles. |
| GET | `/get-chat-title/{chatId}` | none | Get the title of a chat by ID. |
| PUT | `/update-title` | `{ "chatId": "...", "title": "New Title" }` | Rename a chat. |
| PUT | `/add-user-to-chat` | `{ "userId": "...", "chatId": "..." }` | Add a user to an existing chat. |
| PUT | `/delete-user-from-chat` | `{ "userId": "...", "chatId": "..." }` | Remove a user from a chat and close their active socket if connected. |
| GET | `/allowed-users/{chatId}` | none | Retrieve the list of user IDs allowed in a chat. |
| DELETE | `delete-user?deleteUserId={userId}` | none | Delete user connection to all chats |

## Payload models

- `ChatModel`
  - `userIds`: array of user UUID strings
  - `title`: chat title

- `TitleModel`
  - `chatId`: chat UUID
  - `title`: new chat title

- `AlterUserModel`
  - `userId`: user UUID
  - `chatId`: chat UUID

## Notes

- The service does not authenticate requests itself; it assumes the caller forwards valid user identifiers.
- Membership is enforced for WebSocket connections, but REST endpoints are not protected by a separate user session layer in this service.
- `create-chat` generates a new UUID for `chatId` and stores membership in `allowed_users`.

# WebSocket-API

The chat-service supports two WebSocket channels:

- `/ws/{chatId}?userId={userId}` — live chat message stream
- `/ws/notifications/{userId}` — unread message notifications

## Chat socket

### Connect

- Path: `/ws/{chatId}`
- Query: `userId=<userId>`
- Protocol: WebSocket
- Authorization: the user must appear in `allowed_users` for the chat

### Behavior

- On connect, the server streams existing chat history messages as raw binary frames.
- Incoming messages are accepted as binary payloads.
- Messages are persisted to PostgreSQL as raw `protobuf` bytes.
- Each message is broadcast to other connected chat members in the same chat room.
- If the sender is removed from the chat while connected, the server closes their socket.

### Close conditions

- client closes the connection
- the connection is not a valid WebSocket upgrade
- the user is not allowed in the chat
- the user is removed from the chat while connected

## Notification socket

### Connect

- Path: `/ws/notifications/{userId}`
- Protocol: WebSocket
- The server stores one active notification socket per user.

### Server messages

- `unread_counts`

Example payload:

```json
{
  "type": "unread_counts",
  "data": {
    "<chatId>": 3,
    "<chatId2>": 0
  }
}
```

### Client commands

The client may send JSON text messages to update read state.

- Mark a specific chat as read:

```json
{
  "type": "mark_chat_as_read",
  "chatId": "..."
}
```

- Mark all chats as read:

```json
{
  "type": "mark_all_as_read" }
```

## Unread count updates

- When a chat message is delivered, the service broadcasts updated unread counts to connected users in the chat via the notification socket.
- `notificationSockets` are kept in memory and updated for active users only.


# Architecture

The Chat Service is a lightweight ASP.NET Core application that provides real-time chat and notification features.

## Startup flow

1. Load `CHAT_DB_CONNECTION_STRING` from environment variables.
2. Run PostgreSQL migrations from the `migrations` folder using `MigrationRunner`.
3. Configure CORS to allow requests from:
   - `localhost`
   - `apigateway-service`
   - `*.student.inf.st.hsleiden.nl`
4. Enable WebSockets and register HTTP and WebSocket endpoints.

## Core components

- `Program.cs`
  - boots the application
  - runs database migrations
  - configures CORS and WebSocket support
  - initializes in-memory connection maps

- `ChatEndpoints.cs`
  - exposes REST endpoints for chat creation, metadata, and membership management

- `ChatWebSocketHandler.cs`
  - handles WebSocket connections for live chat delivery
  - streams chat history on connect
  - saves incoming messages and broadcasts them to active peers

- `NotificationWebSocketHandler.cs`
  - manages notification WebSocket connections per user
  - sends unread counts and handles read-state commands

- `DatabaseConnection.cs`
  - connects to PostgreSQL using `Npgsql`
  - stores chat metadata, membership, messages, and read state

## Concurrency and runtime behavior

The service keeps live socket state in memory using:

- `ConcurrentDictionary<string, List<WebSocket>> lobbies`
  - group chat connections by `chatId`
- `ConcurrentDictionary<string, WebSocket> userSockets`
  - map `userId:chatId` to active socket
- `ConcurrentDictionary<string, WebSocket> notificationSockets`
  - map `userId` to notification socket

When a chat message arrives:

- the service saves the raw binary payload to `chat_item.protobuf`
- it marks the sender's chat state as read
- it broadcasts the message to other active participants
- it updates unread counts for connected users via notification sockets

## Security considerations

- WebSocket access is gated by membership checks using `allowed_users`
- HTTP endpoint authorization is minimal and assumes the caller uses valid user IDs
- Sensitive data such as database credentials must be provided at runtime through environment variables

# Database-Schema

The chat-service stores chat metadata, membership, messages, and read state in PostgreSQL.

## Tables

### `chat`

- `chat_id` UUID PRIMARY KEY
- `title` text

### `chat_item`

- `chat_item_id` UUID PRIMARY KEY
- `chat_id` UUID REFERENCES `chat`
- `protobuf` bytea
- `created_at` timestamptz NOT NULL DEFAULT NOW()

Notes:
- chat messages are stored as raw binary `protobuf` payloads.
- `created_at` is used for chat history ordering.

### `allowed_users`

- `allowed_user_id` UUID PRIMARY KEY
- `chat_id` UUID
- `user_id` UUID

Notes:
- membership grants users access to chat WebSockets
- duplicate memberships are ignored via `ON CONFLICT DO NOTHING`

### `user_chat_state`

- `user_id` UUID NOT NULL
- `chat_id` UUID NOT NULL
- `last_read_at` timestamptz NOT NULL DEFAULT NOW()
- PRIMARY KEY (`user_id`, `chat_id`)

Notes:
- stores read timestamps per user/chat
- used by `GetUnreadCountsForUser`, `MarkChatAsRead`, and `MarkAllChatsAsRead`

## Migrations

- The app runs all `.sql` files found in `apps/chat-service/migrations` at startup.
- The current migration file adds the `created_at` and `user_id` columns to `chat_item` and creates `user_chat_state`.
- Ensure the database schema is compatible with the version expected by the chat-service.

# Deployment

## Environment variables

- `CHAT_DB_CONNECTION_STRING`
  - required
  - connection string used by `Npgsql` to connect to PostgreSQL

## Docker Compose

The service is defined in the repository `docker-compose.yml` as `chat-service`.

- Container name: `chat-service`
- Host port: `29294`
- Container port: `29294`

### Example environment snippet

```yaml
chat-service:
  build:
    context: ./apps/chat-service
  container_name: chat-service
  ports:
    - "29294:29294"
  environment:
    - CHAT_DB_CONNECTION_STRING=${CHAT_DB_CONNECTION_STRING}
```

## Build and run locally

From `apps/chat-service`:

```bash
dotnet build
dotnet run
```

The service starts on the port configured by ASP.NET Core defaults and is exposed as `29294` in the repository's Docker setup.

## Runtime behavior

- The service will fail to start if `CHAT_DB_CONNECTION_STRING` is missing.
- Database migrations run before the web host starts.
- CORS is configured to allow local development origins and the API gateway host.

## Notes

- The chat-service does not expose its own HTTP authentication layer; it expects upstream services or clients to handle authentication and to pass valid user identifiers.
- The application uses .NET 10 and depends on packages: `Google.Protobuf`, `Grpc.Tools`, `Microsoft.AspNetCore.OpenApi`, and `Npgsql`.

