---
sidebar_position: 3
---

# HTTP API

## Service Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/profile/create` | bearer token | Create a profile for the authenticated user. |
| `PUT` | `/api/profile/update` | bearer token | Update the authenticated user's profile. |
| `DELETE` | `/api/profile/delete/{userId}` | bearer token | Delete a profile by user ID. |
| `GET` | `/api/profile/{userId}` | none | Get a profile by user ID. |
| `GET` | `/api/profile/by-username/{username}` | none | Get a profile by username. |
| `GET` | `/api/profile/all` | none | List all profiles (username and profile picture URL only). |

## Gateway Routes

| Gateway method | Gateway path | Service path |
| --- | --- | --- |
| `POST` | `/api/profile/create` | `POST /api/profile/create` |
| `PUT` | `/api/profile/update` | `PUT /api/profile/update` |
| `DELETE` | `/api/profile/delete/{userId}` | `DELETE /api/profile/delete/{userId}` |
| `GET` | `/api/profile/{userId}` | `GET /api/profile/{userId}` |
| `GET` | `/api/profile/by-username/{username}` | `GET /api/profile/by-username/{username}` |
| `GET` | `/api/profile/all` | `GET /api/profile/all` |

## Payloads

Create/Update request:

```json
{
  "username": "johndoe",
  "name": "John Doe",
  "biography": "Software developer and hobbyist photographer.",
  "mediaPointer": "https://example.com/avatar.jpg",
  "link": "https://johndoe.dev",
  "location": "Copenhagen, Denmark"
}
```

Profile response:

```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "johndoe",
  "name": "John Doe",
  "biography": "Software developer and hobbyist photographer.",
  "mediaPointer": "https://example.com/avatar.jpg",
  "link": "https://johndoe.dev",
  "location": "Copenhagen, Denmark",
  "lastEdited": "2026-06-15T10:00:00Z",
  "created_at": "2026-06-15T10:00:00Z"
}
```

List all profiles response:

```json
[
  {
    "username": "johndoe",
    "profilePictureUrl": "https://example.com/avatar.jpg"
  }
]
```

## Notes

- Create and update extract the user ID from the JWT token, not from the request body.
- Username must be unique (database constraint).
- `GET /profile/**` endpoints are publicly accessible without authentication.
- The `mediaPointer` field is validated for allowed media URL patterns.
- The `location` field is validated for allowed location formats.
- Delete requires the target `userId` as a path parameter and can be performed by any authenticated caller.
- During registration, profile creation is triggered by the API Gateway's `RegistrationConsumer` via RabbitMQ, using the JWT obtained from the auth service.
