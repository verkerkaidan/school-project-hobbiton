---
sidebar_position: 3
---

# HTTP API

The interaction-service exposes REST endpoints for like/comment creation, retrieval, batch lookup, and deletion.

# Endpoints

| Method   | Path                                           | Body         | Returns                     | Description                                                                                             |
|----------|------------------------------------------------|--------------|-----------------------------|---------------------------------------------------------------------------------------------------------|
| `GET`    | `/api/interaction/{userId}/{referenceId}`      | none         | `boolean`                   | Checks if the given user has already liked the given post or comment                                    |
| `GET`    | `/api/interaction/{referenceId}`               | none         | `InteractionReturnDTO`      | Retrieves likes and comments correlated to the given post or comment id                                 |
| `GET`    | `/api/interaction/likes/{referenceId}`         | none         | `List<UUID>`                | Retrieves the ids of all users who have liked the given post or comment                                 |
| `POST`   | `/api/interaction`                             | `CommentDTO` | `UUID`                      | Creates a comment for the given user and post/comment                                                   |
| `POST`   | `/api/interaction/batch`                       | `List<UUID>` | `List<BatchInteractionDTO>` | Bulk fetches likes and comments for reference ids                                                       |
| `POST`   | `/api/interaction/likes/batch?userId={userId}` | `List<UUID>` | `Set<UUID>`                 | Bulk checks which reference ids were liked by the given user                                            |
| `PUT`    | `/api/interaction/{userId}/{referenceId}`      | none         | `boolean`                   | Creates or deletes the like for the given user and comment/post                                         |
| `DELETE` | `/api/interaction/{referenceId}`               | none         | `UUID`                      | Deletes the given comment and all likes/comments that are correlated with it                            |
| `DELETE` | `/api/interaction/post`                        | `List<UUID>` | `List<UUID>`                | Deletes interaction data for the given post reference ids                                               |
| `DELETE` | `/api/interaction/user/{userId}`               | none         | `UUID`                      | Recursively deletes all likes/comments that are correlated to the given user and those deleted comments |

# Models

- `InteractionReturnDTO`
  - `likes` integer
  - `comments` `List<Comment>`
- `Comment`
  - `id` UUID
  - `referenceId` UUID
  - `userId` UUID
  - `comment` string
- `CommentDTO`
  - `userId` UUID
  - `referenceId` UUID
  - `comment` string
- `BatchInteractionDTO`
  - `referenceId` UUID
  - `likes` integer
  - `comments` `List<Comment>`

## Notes

- The service does not authenticate requests itself; it assumes the gateway/internal caller supplies valid identifiers.
