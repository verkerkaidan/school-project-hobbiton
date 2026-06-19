---
sidebar_position: 1
---

# Post Service

The **Post Service** manages the creation, retrieval, and deletion of user posts across multiple hobby types. It supports gym workouts, reading logs, and generic hobby posts.

## Port

| Container port | Host port |
| -------------- | --------- |
| `29293`        | `29293`   |

## Responsibilities

- CRUD operations for posts (create, read, update, delete)
- Polymorphic post types: Gym, Reading, Other
- Attach media references and linked hobby items to posts
- Paginated feed retrieval with user exclusion

## Environment Variables

| Variable                     | Description                       |
| ---------------------------- | --------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                       |
| `CORS_ALLOWED_ORIGINS`       | Comma-separated CORS origins      |

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.2.5
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
- **ORM:** Hibernate with JPA (JOINED inheritance strategy)
