---
sidebar_position: 1
---

# API Gateway

The API Gateway (`apigateway`) is a **Spring Boot** service that acts as the single entry point for all client requests, routing traffic to the appropriate downstream microservices.

## Port

| Container port | Host port |
| -------------- | --------- |
| `23738`        | `23738`   |

## Responsibilities

- Route incoming HTTP requests to backend services
- Centralise cross-cutting concerns (CORS, logging, etc.)

## Environment Variables

| Variable                         | Description                           |
| -------------------------------- | ------------------------------------- |
| `MEDIA_SERVICE_URL`              | URL of the Media Service              |
| `POST_SERVICE_URL`               | URL of the Post Service               |
| `RECOGNITION_SERVICE_URL`        | URL of the Recognition Service        |
| `PROFILE_SERVICE_URL`            | URL of the Profile Service            |
| `PROFILE_CONNECTION_SERVICE_URL` | URL of the Profile Connection Service |
| `NOTIFICATION_SERVICE_URL`       | URL of the Notification Service       |

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
