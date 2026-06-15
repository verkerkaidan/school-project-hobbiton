---
sidebar_position: 3
---

# HTTP API

The media-service exposes REST endpoints for media uploading, report creation, retrieval and deletion

## Endpoints

| Subject | Method   | Path                                          | Body                                                         | Returns                                                                                                                          | Description                                                                                                                                 |
|---------|----------|-----------------------------------------------|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Media   | `GET`    | `/media/file?instances[]=...?instances[]=...` | none                                                         | `[{relevantId: UUID, fileData: string, contentType: string, contentFlag: ContentFlag}]`                                          | Retrieves media files from a list of instance identifiers. Missing files are skipped                                                        |
|         | `GET`    | `/media`                                      | none                                                         | `[{file: string, hash: string}]`                                                                                                 | Retrieves all files with ContentFlag == "UNASSIGNED"                                                                                        |
|         | `POST`   | `/media`                                      | `formdata:`<br/>`"owner_id", "files[]"`                      | `{results: [id: UUID, status: string]}`                                                                                          | Uploads one or more media files                                                                                                             |
|         | `PUT`    | `/media`                                      | `{[{contentFlag: ContentFlag, hash: string, note: string}]}` | `{results: [id: UUID, status: string]}`                                                                                          | Updates all given files with the given ContentFlags. Note is used to differentiate ai from admin changes. Could be used to notify reporters |
|         | `DELETE` | `/media/file/{instance_id}`                   | none                                                         | `{id: UUID}`                                                                                                                     | Deletes the given instance, and if necessary the correlated file and reports                                                                |
|         | `DELETE` | `/media/admin`                                | `{hash: string}`                                             | `{id: UUID}`                                                                                                                     | Deletes the given media file, bans it, and deletes all correlated instances and reports                                                     |
| Report  | `GET`    | `/report`                                     | none                                                         | `{[{hash: string, contentFlag: ContentFlag, file: string, contentType: string}]}`                                                | Retrieves all files with one or more reports correlated with them                                                                           |
|         | `POST`   | `/report/{user_id}/{instance_id}`             | none                                                         | `{report_id: UUID, status: string}`                                                                                              | Creates a report for the file correlated with the given instance_id                                                                         |
|         | `DELETE` | `/report/{report_id}`                         | none                                                         | `{report_id: UUID, status: string}`                                                                                              | Deletes the given report                                                                                                                    |
| User    | `GET`    | `/user/reports/{owner_id}`                    | none                                                         | `{reports: [{id: UUID, file_id: UUID, user_id: UUID]}`                                                                           | Retrieve all reports correlated to the given user                                                                                           |
|         | `GET`    | `/user/{owner_id}`                            | none                                                         | `{results: [{instance_id: UUID, status: string, media_file: {contentFlag: ContentFlag, fileData: string, contentType: string}}]` | Retrieves all media files correlated to the given user                                                                                      |
|         | `DELETE`  | `/user/{owner_id}`                            | none                                                         | `{instances: [{instance_id: UUID, status: string}], reports: [{report_id: UUID, status: string}]}`                               | Deletes all instances and reports correlated to the given user. Media_files and their reports are deleted accordingly                       |

# Models

- ContentFlag:
  - `SAFE` 0
  - `RISK` 1
  - `UNASSIGNED` 2 default

## Notes

- The service does not authenticate requests itself; it assumes the caller forwards valid user identifiers.
