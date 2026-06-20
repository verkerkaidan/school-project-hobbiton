---
sidebar_position: 1
---

# Profile Service

The **Profile Service** stores and serves user profile data, including bios, avatars, and hobby tags. Profile creation is called by the API Gateway's registration consumer as part of the RabbitMQ-based registration saga.

## Port

| Container port | Host port |
| -------------- | --------- |
| `21526`        | `21526`   |

All endpoints are served under the `/api` context path.

## Responsibilities

- CRUD for user profiles
- Avatar and profile image management
- Hobby and interest tags
- Batch profile lookup by user ID list
- Fuzzy username search
- Profile creation is triggered by the API Gateway's registration consumer via RabbitMQ

## Environment Variables

| Variable | Required | Description |
| -------- | -------- | ----------- |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC connection string. In Docker: `jdbc:postgresql://database:5432/{db}`. |
| `SPRING_DATASOURCE_USERNAME` | Yes | Service database username. |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Service database password. |
| `JWT_SECRET` | Yes | HMAC-SHA256 signing secret for JWT validation. |
| `JWT_ISSUER` | Yes | JWT issuer claim. |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated CORS origins, defaults to `http://localhost:23738`. |
| `ADMIN_TEST_USER` | No | Seed admin username (reserved). |
| `ADMIN_TEST_PASSWORD` | No | Seed admin password (reserved). |

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.2.5
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate (`ddl-auto=update`)
- **Auth:** Auth0 `java-jwt` + `jjwt` for JWT validation
- **Monitoring:** Spring Actuator + Micrometer + Prometheus
