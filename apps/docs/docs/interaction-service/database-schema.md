---
sidebar_position: 4
---

# Database Schema

The interaction-service stores metadata and ownership of likes and comments in PostgreSQL

## Tables

### `comment`

- `id` UUID
- `reference_id` UUID
- `user_id` UUID
- `comment` string

### `custom_like`

- `id` UUID
- `reference_id` UUID
- `user_id` UUID