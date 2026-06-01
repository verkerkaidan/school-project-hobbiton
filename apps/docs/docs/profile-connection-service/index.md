---
sidebar_position: 1
---

# Profile Connection Service

The **Profile Connection Service** manages the social graph — follow and friend relationships between users.

## Port

| Container port | Host port |
| -------------- | --------- |
| `29574`        | `29574`   |

## Responsibilities

- Follow / unfollow users
- Query followers and following lists
- Trigger notifications when a user gains a new follower

## Environment Variables

| Variable                     | Description                       |
| ---------------------------- | --------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                       |
| `NOTIFICATION_SERVICE_URL`   | URL of the Notification Service   |
| `PROFILE_SERVICE_URL`        | URL of the Profile Service        |

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
