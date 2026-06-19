---
sidebar_position: 3
---

# HTTP API

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
