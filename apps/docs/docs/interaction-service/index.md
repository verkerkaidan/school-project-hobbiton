---
sidebar_position: 1
---

# Interaction Service

The **Interaction Service** manages user interactions such as likes, comments, and reactions on posts.

## Port

| Container port | Host port |
| -------------- | --------- |
| `27087`        | `27087`   |

## Responsibilities

- Record and retrieve likes / reactions
- Manage comments on posts
- Expose interaction counts to other services

## Environment Variables

| Variable                     | Description                       |
| ---------------------------- | --------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                       |

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
