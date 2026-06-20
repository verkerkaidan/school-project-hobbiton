---
sidebar_position: 3
---

# HTTP API

All endpoints are under the `/api/profile` base path.

## Service Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/profile/create` | bearer token | Create a profile for the authenticated user. |
| `PUT` | `/api/profile/update` | bearer token | Update the authenticated user's profile. |
| `DELETE` | `/api/profile/delete/{userId}` | bearer token | Delete a profile by user ID. |
| `GET` | `/api/profile/{userId}` | none | Get a profile by user ID. |
| `GET` | `/api/profile/by-username/{username}` | none | Get a profile by username. |
| `GET` | `/api/profile/all` | none | List all profiles (userId, username, mediaPointer). |
| `POST` | `/api/profile/batch` | none | Get profile summaries by a list of UUIDs. |
| `GET` | `/api/profile/search-profiles/{query}` | none | Fuzzy search profiles by username. |

## Gateway Routes

The API Gateway forwards requests directly to the same paths (`/api/profile/*`).

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
    "userId": "a1b2c3d4-...",
    "username": "johndoe",
    "profilePictureUrl": "https://example.com/avatar.jpg"
  }
]
```

Batch request:

```json
["a1b2c3d4-...", "e5f6-7890-abcd-ef1234567890"]
```

Batch response:

```json
[
  {
    "userId": "a1b2c3d4-...",
    "username": "johndoe",
    "profilePictureUrl": "https://example.com/avatar.jpg"
  }
]
```

Search response:

```json
[
  {
    "userId": "a1b2c3d4-...",
    "username": "johndoe",
    "profilePictureUrl": "https://example.com/avatar.jpg"
  }
]
```

## Notes

- Create and update extract the user ID from the JWT token, not from the request body.
- Username must be unique (database constraint).
- `GET /api/profile/**` endpoints are publicly accessible without authentication.
- `POST /api/profile/create` and `POST /api/profile/update` require a valid JWT Bearer token.
- The `mediaPointer` field is validated for allowed media URL patterns.
- The `location` field is validated for allowed location formats.
- Delete requires the target `userId` as a path parameter and can be performed by any authenticated caller.
- During registration, profile creation is triggered by the API Gateway's `RegistrationConsumer` via RabbitMQ, using the JWT obtained from the auth service.
