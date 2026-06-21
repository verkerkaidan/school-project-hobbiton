---
sidebar_position: 5
---

# Deployment

## Docker Compose

The service is defined as `profile-connection-service` in both local and production compose files.

| Setting | Value |
| --- | --- |
| Build context | `./apps/profile-connection-service` |
| Container name | `profile-connection-service` |
| Port mapping | `29574:29574` |
| CPU limit | `0.5` |
| Memory limit | `512m` |
| Network | `microservices-network` |
| Dependency | `database` with `service_healthy` |

## Environment Variables

| Variable | Description |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL, e.g. `jdbc:postgresql://database:5432/${SPRING_DATASOURCE_PROFILE_CONNECTION_DATABASE}`. |
| `SPRING_DATASOURCE_USERNAME` | Profile connection database username. |
| `SPRING_DATASOURCE_PASSWORD` | Profile connection database password. |
| `NOTIFICATION_SERVICE_URL` | URL of the Notification Service. Present in compose wiring for cross-service notification use. |
| `PROFILE_SERVICE_URL` | URL of the Profile Service. |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed CORS origins. In compose this uses `${MIDDLEWARE_ALLOWED_ORIGINS}`. |

## Application Settings

| Setting | Value |
| --- | --- |
| `server.port` | `29574` |
| `server.servlet.context-path` | `/api` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `spring.jpa.properties.hibernate.dialect` | `org.hibernate.dialect.PostgreSQLDialect` |
| `management.endpoints.web.exposure.include` | `health,prometheus` |
| `management.endpoints.web.base-path` | `/actuator` |
| `management.metrics.tags.application` | `profile-connection-service` |

## Runtime Notes

- The database schema is managed by Hibernate rather than explicit SQL migration files.
- The local seeder only runs when no profiles are present.
- The API Gateway uses `PROFILE_CONNECTION_SERVICE_URL` to call this service.
