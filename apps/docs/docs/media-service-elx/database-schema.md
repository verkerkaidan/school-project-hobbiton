---
sidebar_position: 4
---

# Database Schema

The media-service stores media metadata, ownership and report information in PostgreSQL.

## Tables

### `media_files`

- `id` binary_id PRIMARY KEY
- `content_flag` integer
- `content_type` text
- `hash` binary
- `original` binary
- `admin_approved` boolean, default: false

`content_flag` values:
- `0` = `SAFE`
- `1` = `RISK`
- `2` = `UNASSIGNED`

### `media_instances`

- `id` binary_id PRIMARY KEY
- `file_id` binary_id
- `owner_id` binary_id

### `file_report`

- `id` binary_id PRIMARY KEY
- `file_id` binary_id
- `user_id` binary_id

### `reported_file`

- `id` binary_id PRIMARY KEY
- `hash` binary

## Migrations

- The app runs all `.exs` files found in `apps/media_service_elx/priv/repo/migrations` at startup.
- Ensures the database schema is compatible with the version expected by the media_service_elx.
