---
sidebar_position: 4
---

# Database Schema

The media-service stores media metadata, ownership and report information in PostgreSQL.

## Tables

### `media_files`

- `id` binary_id PRIMARY KEY
- `content_flag` content_flag
- `content_type` text
- `hash` binary
- `original` binary
- `admin_approved` boolean, default: false

:content_flag is an enum:
- `SAFE`
- `RISK`
- `UNASSIGNED`

### `media_instances`

- `id` binary_id PRIMARY KEY
- `file_id` binary
- `owner_id` binary

### `file_report`

- `id` binary_id PRIMARY KEY
- `file_id` binary
- `user_id` binary

### `reported_file`

- `id` binary_id PRIMARY KEY
- `hash` binary

## Migrations

- The app runs all `.exs` files found in `apps/media_service_elx/priv/repo/migrations` at startup.
- Ensures the database schema is compatible with the version expected by the media_service_elx.
