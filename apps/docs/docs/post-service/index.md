---
sidebar_position: 1
---

# Post Service

The **Post Service** manages the creation, retrieval, and deletion of user posts.

## Port

| Container port | Host port |
| -------------- | --------- |
| `29293`        | `29293`   |

## Responsibilities

- CRUD operations for posts
- Attach media references to posts
- Expose posts to the feed and profile services

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
