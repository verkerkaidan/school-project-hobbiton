---
sidebar_position: 3
---

# HTTP API

The interaction-service exposes REST endpoints for like/comment creation, updating, retrieval and deletion

# Endpoints

| Method   | Path                                  | Body         | Returns                                | Description                                                                                             |
|----------|---------------------------------------|--------------|----------------------------------------|---------------------------------------------------------------------------------------------------------|
| `GET`    | `/interaction/{userId}/{referenceId}` | none         | `ResponseEntity<Boolean>`              | Checks if the given user has already liked the given post or comment                                    |
| `GET`    | `/interaction/{referenceId}`          | none         | `ResponseEntity<InteractionReturnDTO>` | Retrieves all interaction correlated to the given post or comment id                                    |
| `GET`    | `/interaction/likes/{referenceId}`    | none         | `ResponseEntity<List<UUID>>`           | Retrieves the id's of all users who have liked the given post or comment                                |
| `POST`   | `/interaction`                        | `CommentDTO` | `ResponseEntity<UUID>`                 | Creates a comment for the given user and post/comment                                                   |
| `PUT`    | `/interaction/{userId}/{referenceId}` | none         | `ResponseEntity<Boolean>`              | Creates or deletes the like for the given user and comment/post                                         |
| `DELETE` | `/interaction/{referenceId}`          | none         | `ResponseEntity<UUID>`                 | Deletes the given comment and all likes/comments that are correlated with it                            |
| `DELETE`  | `/interaction/user/{userId}`           | none         | `ResponseEntity<UUID>`                  | Recursively deletes all likes/comments that are correlated to the given user and those deleted comments |

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

## Notes

- The service does not authenticate requests itself; it assumes the caller forwards valid user identifiers.
