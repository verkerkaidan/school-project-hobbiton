---
sidebar_position: 1
---

# Media Service

The **Media Service** (Spring Boot) handles file uploads and media asset management.

## Port

| Container port | Host port |
| -------------- | --------- |
| `27086`        | `27086`   |

## Responsibilities

- Accept multipart file uploads
- Store and serve media assets
- Track media metadata in PostgreSQL

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
