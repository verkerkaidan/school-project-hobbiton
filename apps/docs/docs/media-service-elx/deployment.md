---
sidebar_position: 5
---

# Deployment & Configuration

## Environment variables

- `PORT`
  - String used to set port value at build
- `DB_HOST`
  - String used to connect to the database
- `DB_PORT`
    - String is converted to int to be used to connect to the database
- `DB_NAME`
    - String used to access the media_database
- `DB_USERNAME`
    - String used to get rights in to media_database
- `DB_PASSWORD`
    - String used to get rights in to media_database
- `SECRET_KEY_BASE`
    - String used to set up Phoenix's connection to the database
- `CORS_ALLOWED_ORIGINS`
  - String containing all origins that are allowed for CORS


All variables except for secret_key_base have fallbacks for local development.

## Docker Compose

The service is defined in the repository `docker-compose.yml` as `media-service-elx`.

- Container name: `media-service`
- Host port: `27086`
- Container port: `27086`

### Example environment snippet

```yaml
media-service-elx:
  build:
    context: ./apps/media_service_elx
  container_name: media-service
  ports:
    - "27086:27086"
  environment:
    PORT: "27086"
    DB_HOST: database
    DB_PORT: 5432
    DB_NAME: ${SPRING_DATASOURCE_MEDIA_DATABASE}
    DB_USERNAME: ${SPRING_DATASOURCE_USERNAME_MEDIA}
    DB_PASSWORD: ${SPRING_DATASOURCE_PASSWORD_MEDIA}
    SECRET_KEY_BASE: ${SECRET_KEY_BASE}
```

## Build and run locally

From `apps/media_service_elx`:

```bash
mix compile
mix phx.server
```

The service starts on the port configured by `config/runtime.exs` and is exposed as `27086` in the repository's Docker setup.
If the environment variable for `PORT` is missing, the media-service-elx is exposed as `27089` in the repository's Docker setup.

## Runtime behavior

- Database migrations run before the web host starts.
- CORS is configured to allow local development origins and the API gateway host.
