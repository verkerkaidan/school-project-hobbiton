---
sidebar_position: 5
---

# Database Schema

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
- `user_id` UUID

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
