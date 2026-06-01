---
sidebar_position: 1
---

# Profile Service

The **Profile Service** stores and serves user profile data, including bios, avatars, and hobby tags.

## Port

| Container port | Host port |
| -------------- | --------- |
| `21526`        | `21526`   |

## Responsibilities

- CRUD for user profiles
- Avatar and profile image management
- Hobby and interest tags

## Environment Variables

| Variable                     | Description                       |
| ---------------------------- | --------------------------------- |
| `DB_HOST`                    | PostgreSQL hostname               |
| `DB_PORT`                    | PostgreSQL port                   |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                       |
| `JWT_ISSUER`                 | JWT issuer claim                  |
| `JWT_SECRET`                 | JWT signing secret                |
| `ADMIN_TEST_USER`            | Seed admin username               |
| `ADMIN_TEST_PASSWORD`        | Seed admin password               |

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Gradle (`gradlew`) + Maven (`mvnw`)
- **Database:** PostgreSQL
