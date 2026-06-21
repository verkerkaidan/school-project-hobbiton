---
sidebar_position: 2
---

# Interaction Service Architecture

The Interaction Service is a Java Springboot application that handles all likes and comments that users leave on the platform.

## Startup flow

1. Load `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` and `CORS_ALLOWED_ORIGINS` from environment variables
2. Let Hibernate update the PostgreSQL schema with `spring.jpa.hibernate.ddl-auto=update`
3. Start the Spring Boot endpoint on port `27087` with the `/api` context path

## Core components

- `SecurityConfig`
    - Configures CORS to allow requests from the origin marked in the ci/cd
- `Application.properties`
    - Configures environment variables into local variables
    - Defines the service endpoint
- `InteractionController`
  - Handles like, comment, batch lookup, and delete requests
- `Comment`
  - Defines the database structure for comments
- `CustomLike`
  - Defines the database structure for likes

## Concurrency and runtime behavior

- The service leverages Java's multithreading capabilities and the Spring Boot framework to handle concurrent requests efficiently.
- Spring handles concurrent HTTP requests on request threads.

## Security considerations

- The service disables CSRF and permits all requests in its own Spring Security configuration because it is intended to sit behind the gateway/internal network.
- It uses CORS configuration to restrict access to trusted origins, reducing the risk of cross-origin attacks.
- The service is designed for gateway/internal use and does not authenticate user ids itself.
