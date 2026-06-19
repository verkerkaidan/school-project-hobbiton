---
sidebar_position: 5
---

# Database Schema

The schema is managed by Hibernate `ddl-auto=update` — no manual SQL migrations are used. All tables live in the `posts_db` database.

## Tables

### `post` (base table)

| Column       | Type        | Constraints |
|------------- |-------------|-------------|
| `id`         | `uuid`      | PK          |
| `description`| `text`      | NOT NULL    |
| `title`      | `varchar`   | NOT NULL    |
| `user_id`    | `uuid`      | NOT NULL    |
| `created_at` | `timestamp` | NOT NULL    |
| `updated_at` | `timestamp` |             |
| `hobby_type` | `varchar`   |             |

### `gym` (extends `post`)

| Column          | Type     | Constraints              |
|-----------------|----------|--------------------------|
| `id`            | `uuid`   | PK + FK → `post(id)`     |
| `total_duration`| `float8` | Duration in minutes      |

### `reading` (extends `post`)

| Column            | Type      | Constraints              |
|-------------------|-----------|--------------------------|
| `id`              | `uuid`    | PK + FK → `post(id)`     |
| `book_title`      | `varchar` |                          |
| `pages_read`      | `varchar` |                          |
| `total_pages_read`| `varchar` |                          |
| `book_finished`   | `boolean` |                          |

### `other` (extends `post`)

| Column | Type   | Constraints              |
|--------|--------|--------------------------|
| `id`   | `uuid` | PK + FK → `post(id)`     |

### `gym_exercise`

| Column  | Type     | Constraints              |
|---------|----------|--------------------------|
| `id`    | `uuid`   | PK                       |
| `post`  | `uuid`   | FK → `gym(id)`          |
| `title` | `varchar`|                          |
| `reps`  | `int`    |                          |
| `weight`| `float8` |                          |
| `rpe`   | `float8` | Rate of Perceived Exertion |

### `linked_hobby_item`

| Column       | Type     | Constraints              |
|------------- |----------|--------------------------|
| `id`         | `uuid`   | PK                       |
| `post_id`    | `uuid`   | FK → `post(id)`          |
| `source`     | `varchar`| e.g. `LIBRARY`, `MOVIE_API` |
| `external_id`| `varchar`|                          |
| `hobby_type` | `varchar`| e.g. `BOOK`, `MOVIE`     |
| `display_name`| `varchar`|                         |
| `image_url`  | `varchar`|                          |

### `media_instance`

| Column      | Type   | Constraints              |
|-------------|--------|--------------------------|
| `id`        | `uuid` | PK                       |
| `instance_id`| `uuid` | NOT NULL — references media service |
| `post`      | `uuid` | FK → `post(id)`, NOT NULL |
