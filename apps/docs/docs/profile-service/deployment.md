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

| Variable | Required | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC connection string. In Docker: `jdbc:postgresql://database:5432/{db}`. |
| `SPRING_DATASOURCE_USERNAME` | Yes | Service database username. |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Service database password. |
| `JWT_SECRET` | Yes | HMAC-SHA256 signing secret for JWT validation. |
| `JWT_ISSUER` | Yes | JWT issuer claim. |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated CORS origins, defaults to `http://localhost:23738`. |
| `ADMIN_TEST_USER` | No | Seed admin username (reserved). |
| `ADMIN_TEST_PASSWORD` | No | Seed admin password (reserved). |

## Build and run locally

From `apps/profile-service`:

```bash
./mvnw spring-boot:run          # run with Maven
./mvnw clean package -DskipTests   # build a JAR
java -jar target/*.jar             # run the packaged JAR
```

## Docker Build

Multi-stage Dockerfile:

1. **Build stage** — `eclipse-temurin:21-jdk`, caches Maven dependencies, runs `mvn clean package -DskipTests`.
2. **Runtime stage** — `eclipse-temurin:21-jre`, runs `java -jar app.jar`.

## Actuator Endpoints

| Endpoint | Path | Description |
| --- | --- | --- |
| Health | `/api/actuator/health` | Health check with detailed status. |
| Prometheus | `/api/actuator/prometheus` | Prometheus metrics (enabled via Micrometer). |

## Resource Limits

| Environment | CPUs | Memory |
| --- | --- | --- |
| Local | 0.5 | 512m |
| Production | 1 | 1024m |

## Runtime Notes

- Hibernate DDL auto-update (`spring.jpa.hibernate.ddl-auto=update`) synchronises the schema on startup.
- The API Gateway uses `PROFILE_SERVICE_URL` configured as `http://profile-service:21526` to route profile actions.
- CORS is configured to allow requests from the API Gateway origin (`http://localhost:23738` by default).
