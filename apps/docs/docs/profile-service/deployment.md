---
sidebar_position: 5
---

# Deployment

## Docker Compose

The service is defined as `profile-service` in the local compose file.

| Setting | Value |
| --- | --- |
| Build context | `./apps/profile-service` |
| Container name | `profile-service` |
| Port mapping | `21526:21526` |
| CPU limit | `0.5` |
| Memory limit | `512m` |
| Network | `microservices-network` |
| Dependency | `database` with `service_healthy` |

## Environment Variables

| Variable | Description |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection string. In Docker: `jdbc:postgresql://database:5432/{db}`. |
| `SPRING_DATASOURCE_USERNAME` | Service database username. |
| `SPRING_DATASOURCE_PASSWORD` | Service database password. |
| `DB_HOST` | PostgreSQL hostname. In Docker this is `database`. |
| `DB_PORT` | PostgreSQL port, defaults to `5432`. |
| `JWT_SECRET` | HMAC-SHA256 signing secret for JWT validation. |
| `JWT_ISSUER` | JWT issuer claim. |
| `CORS_ALLOWED_ORIGINS` | Comma-separated CORS origin patterns, defaults to `http://localhost:23738`. |
| `ADMIN_TEST_USER` | Seed admin username (reserved). |
| `ADMIN_TEST_PASSWORD` | Seed admin password (reserved). |

## Runtime Notes

- Build uses a multi-stage Dockerfile with `eclipse-temurin:21-jdk` for compilation and `eclipse-temurin:21-jre` for runtime.
- Built with Maven (`mvnw`). Gradle (`gradlew`) is also available in the source directory.
- Hibernate DDL auto-update (`spring.jpa.hibernate.ddl-auto=update`) synchronises the schema on startup.
- The API Gateway uses `PROFILE_SERVICE_URL` configured as `http://profile-service:21526` to route profile actions.
