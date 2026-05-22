---
sidebar_position: 1
---

# Authentication Service

The **Authentication Service** handles user registration, login, token issuance, and OAuth2 integration.

## Port

| Container port | Host port |
| -------------- | --------- |
| `21525`        | `21525`   |

## Responsibilities

- User sign-up and login
- JWT token generation and validation
- OAuth2 / social login (Google, etc.)
- Admin user bootstrap

## Environment Variables

| Variable                     | Description                       |
| ---------------------------- | --------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                       |
| `JWT_ISSUER`                 | JWT issuer claim                  |
| `JWT_SECRET`                 | Secret used to sign JWT tokens    |
| `OAUTH_CLIENT_ID`            | OAuth2 client ID                  |
| `OAUTH_CLIENT_SECRET`        | OAuth2 client secret              |
| `ADMIN_TEST_USER`            | Seed admin username               |
| `ADMIN_TEST_PASSWORD`        | Seed admin password               |

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
