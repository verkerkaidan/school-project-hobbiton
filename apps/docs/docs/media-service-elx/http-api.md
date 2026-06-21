---
sidebar_position: 3
---

# HTTP API

The media-service exposes REST endpoints for media uploading, report creation, retrieval, moderation, and deletion.

## Endpoints

| Subject | Method   | Path                                              | Body                                                       | Returns                                                                                                                          | Auth      | Description                                                                                                            |
|---------|----------|---------------------------------------------------|------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------------|
| Media   | `GET`    | `/api/media/file?instances[]=...&instances[]=...` | none                                                       | `[{relevantId: UUID, fileData: string, contentType: string, contentFlag: ContentFlag}]`                                          | JWT       | Retrieves media files from a list of instance identifiers. Missing files are skipped                                   |
|         | `GET`    | `/api/media`                                      | none                                                       | `[{file: string, hash: string}]`                                                                                                 | none      | Retrieves all files with `ContentFlag == UNASSIGNED`                                                                   |
|         | `POST`   | `/api/media`                                      | `form-data:`<br/>`owner_id`, `files`                       | `{results: [{id: UUID, status: string}]}`                                                                                        | JWT       | Uploads one or more media files                                                                                        |
|         | `PUT`    | `/api/media`                                      | `[{contentFlag: ContentFlag, hash: string, note: string}]` | `{results: [{file_id: UUID, status: string}]}`                                                                                   | none      | Updates all given files with the given content flags. `note` identifies AI/admin changes                               |
|         | `DELETE` | `/api/media/file?instances[]=...&instances[]=...` | none                                                       | `[{id: UUID}]`                                                                                                                   | JWT       | Deletes the given instances, and if necessary the correlated files and reports                                         |
|         | `DELETE` | `/api/media/admin`                                | `{hash: string}`                                           | `{id: UUID}`                                                                                                                     | JWT admin | Deletes the given media file, bans it, and deletes all correlated instances and reports                                |
| Report  | `GET`    | `/api/report`                                     | none                                                       | `[{hash: string, contentFlag: ContentFlag, file: string, contentType: string}]`                                                  | JWT       | Retrieves all files with one or more reports correlated with them                                                      |
|         | `POST`   | `/api/report/{user_id}/{instance_id}`             | none                                                       | `{report_id: UUID, status: string}`                                                                                              | JWT       | Creates a report for the file correlated with the given instance id                                                    |
|         | `DELETE` | `/api/report/{report_id}`                         | none                                                       | `{report_id: UUID, status: string}`                                                                                              | JWT       | Deletes the given report                                                                                               |
| User    | `GET`    | `/api/user/reports/{owner_id}`                    | none                                                       | `{reports: [{id: UUID, file_id: UUID, user_id: UUID}]}`                                                                          | JWT       | Retrieves all reports correlated to the given user                                                                     |
|         | `GET`    | `/api/user/{owner_id}`                            | none                                                       | `{results: [{instance_id: UUID, status: string, media_file: {contentFlag: ContentFlag, fileData: string, contentType: string}}]}` | JWT       | Retrieves all media files correlated to the given user                                                                 |
|         | `DELETE` | `/api/user/{owner_id}`                            | none                                                       | `{instances: [{id: UUID, status: string}], reports: {status: string, result: any}}`                                              | JWT       | Deletes all instances and reports correlated to the given user. Media files and their reports are deleted accordingly  |

# Models

- ContentFlag:
  - `SAFE` 0
  - `RISK` 1
  - `UNASSIGNED` 2 default

## Notes

- JWT-protected routes require an `Authorization: Bearer <token>` header. The token must be HS256 signed and contain `iss`, `userId`, and optional `role` claims.
- `DELETE /api/media/admin` additionally requires `ROLE_ADMIN`.
