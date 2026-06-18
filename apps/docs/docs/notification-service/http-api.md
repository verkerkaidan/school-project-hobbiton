---
sidebar_position: 3
---

# HTTP and WebSocket API

## Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/health` | none | Liveness check. |
| `GET` | `/ws?token=<jwt>` | JWT query param or bearer header | Upgrade to a WebSocket for live notification delivery. |
| `POST` | `/notifications` | internal caller | Create one notification and push it to the recipient if connected. |
| `POST` | `/notifications/batch` | internal caller | Create multiple `USER_POST` notifications for subscribed recipients. |
| `GET` | `/notifications?limit=10&offset=0` | bearer token | List the authenticated user's notifications. |
| `PATCH` | `/notifications/{id}/read` | bearer token | Mark one notification as read for the authenticated user. |
| `POST` | `/notifications/read-all` | bearer token | Mark all unread notifications as read for the authenticated user. |
| `GET` | `/notifications/unread-count` | bearer token | Return `{ "count": number }`. |
| `DELETE` | `/notifications/users/me` | bearer token | Hard-delete all notifications where the authenticated user is the recipient or actor. |
| `DELETE` | `/notifications/users/{userId}` | bearer token (admin) | Hard-delete all notifications for the given user. Requires `ROLE_ADMIN` in the token. |
| `GET` | `/notification-preferences` | bearer token | Return all known notification type preferences, defaulting missing rows to enabled. |
| `PUT` | `/notification-preferences/{type}` | bearer token | Upsert `{ "enabled": boolean }` for a notification type. |
| `GET` | `/user-post-subscriptions/{authorId}` | bearer token | Return whether the authenticated user subscribes to an author's posts. |
| `PUT` | `/user-post-subscriptions/{authorId}` | bearer token | Toggle the authenticated user's post subscription for an author. |

## Create Notification Payload

```json
{
  "userId": "recipient-uuid",
  "actorId": "actor-uuid",
  "type": "LIKE",
  "message": "Someone liked your post",
  "content": "Optional body text",
  "referenceId": "post-or-resource-uuid"
}
```

## Batch Notification Payload

```json
{
  "actorId": "author-uuid",
  "recipientIds": ["recipient-uuid"],
  "type": "USER_POST",
  "message": "A followed author created a post",
  "referenceId": "post-uuid"
}
```

## WebSocket Message

```json
{
  "type": "NOTIFICATION",
  "payload": {
    "id": "notification-uuid",
    "userId": "recipient-uuid",
    "actorId": "actor-uuid",
    "type": "COMMENT",
    "message": "Someone commented on your post",
    "content": "Nice work",
    "referenceId": "post-uuid",
    "readAt": null,
    "createdAt": "2026-06-15T10:00:00Z"
  }
}
```

## Notes

- Browser WebSocket clients pass JWTs through the `token` query parameter because custom headers are not available during browser WebSocket handshakes.
- Missing notification preference rows are treated as enabled.
- `POST /notifications/batch` is intended for internal use and should not be publicly exposed through gateway routing.
