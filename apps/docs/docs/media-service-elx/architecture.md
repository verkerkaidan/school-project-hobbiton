---
sidebar_position: 2
---

# Media Service Architecture

The Media Service is a lightweight Elixir application that provides media upload, report generation, and storage features.

## Startup flow

1. Load `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, and `SECRET_KEY_BASE` from environment variables
2. Run PostgreSQL migrations from the `priv/repo/migrations` folder using `Ecto.Migrator`
3. Start the Phoenix endpoint to listen for incoming HTTP requests on port `27086`

## Core components

- `lib/media_service/endpoint.ex`
  - Defines the Phoenix endpoint
  - Configures the router and plugs
  - Configures CORS to allow requests from the origins marked in the ci/cd
- `lib/media_service/router.ex`
  - Defines the HTTP REST routes for media actions
  - Defines the HTTP REST routes for report actions
  - Defines the HTTP REST routes for user actions
  - Routes requests to the appropriate controllers
- `lib/media_service/controllers/media_controller.ex`
  - Handles media requests
  - Uploading/retrieving/updating/deleting
- `lib/media_service/controllers/report_controller.ex`
  - Handles report requests
  - Uploading/retrieving
- `lib/media_service/controllers/user_controller.ex`
  - Handles user requests
  - Retrieving/deleting
- `config/config.exs`
  - Configures the database connection using `Ecto.Adapters.Postgres`
  - Configures the Phoenix endpoint
- `config/prod.exs`
  - Configures production-specific settings for the database and endpoint
- `config/dev.exs`
  - Configures development-specific settings for the database and endpoint
- `priv/repo/migrations/`
  - Contains Ecto migration files for setting up the database schema

## Concurrency and runtime behavior

- The service leverages Elixir's lightweight processes and the Phoenix framework to handle concurrent requests efficiently.
- Each incoming HTTP request is handled in a separate process, allowing for high throughput and responsiveness.
- The service uses Ecto for database interactions, which provides built-in support for connection pooling and efficient query execution.
- Media uploads are processed asynchronously to avoid blocking the main request flow, allowing for better performance and user experience.

## Security considerations

- The service validates and sanitizes all incoming data to prevent injection attacks and ensure data integrity.
- It uses CORS configuration to restrict access to trusted origins, reducing the risk of cross-origin attacks.
- The service relies on Phoenix's built-in security features, such as CSRF protection and secure session management, to safeguard against common web vulnerabilities.
- Sensitive information, such as database credentials and secret keys, must be provided at runtime through environment variables and should not be hardcoded in the source code
