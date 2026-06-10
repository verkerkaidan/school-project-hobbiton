---
sidebar_position: 4
---

# WebSocket API

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
