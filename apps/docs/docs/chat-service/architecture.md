---
sidebar_position: 2
---

# Chat Service Architecture

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
