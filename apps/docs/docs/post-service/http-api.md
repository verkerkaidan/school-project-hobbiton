---
sidebar_position: 3
---

# HTTP API

All endpoints are prefixed with `/api` and mapped from `@RequestMapping("/posts")`.

## Endpoints

| Method | Path | Description | Parameters |
|--------|------|-------------|------------|
| POST | `/api/posts` | Create a post (if `id` is null) or update (if `id` is present) | Body: `PostDTO` (JSON) |
| GET | `/api/posts/{post_id}` | Get a single post by ID | Path: `post_id` (UUID) |
| GET | `/api/posts/users/{user_id}` | Get all posts by a user | Path: `user_id` (UUID) |
| GET | `/api/posts/hobby/{propertyType}` | Get posts filtered by hobby type | Path: `propertyType` (`GYM`, `READING`, `OTHER`); Query: `page` (int, default 0), `pagesize` (int, default 20) |
| GET | `/api/posts` | Get all posts (paginated) | Query: `page` (int, default 0), `pagesize` (int, default 20), `excludeUserIds` (List of UUID, optional) |
| DELETE | `/api/posts/{post_id}` | Delete a post by ID | Path: `post_id` (UUID) |
| DELETE | `/api/posts/users/{user_id}` | Delete all posts by a user | Path: `user_id` (UUID) |

## Request body (POST/PUT)

The `type` field determines which polymorphic subtype is deserialised.

```json
{
  "type": "GYM",
  "title": "Morning Workout",
  "description": "Great session today",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "mediaInstanceSet": ["uuid1", "uuid2"],
  "linkedItems": [
    {
      "displayName": "Bench Press",
      "imageUrl": "http://example.com/bench.png",
      "externalId": "lib-123",
      "hobbyType": "EXERCISE"
    }
  ],
  "exercises": [
    { "title": "Bench Press", "reps": 10, "weight": 80.0, "rpe": 7.5 }
  ],
  "totalDuration": 45.0
}
```

### Type-specific fields

| Type | Fields |
|------|--------|
| `GYM` | `exercises` (array of `{title, reps, weight, rpe}`), `totalDuration` |
| `READING` | `bookTitle`, `pagesRead`, `totalPagesRead`, `bookFinished` |
| `OTHER` | (none — uses only base `PostDTO` fields) |
