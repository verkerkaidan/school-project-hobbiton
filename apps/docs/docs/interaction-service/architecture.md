---
sidebar_position: 2
---

# Interaction Service Architecture

The Interaction Service is a Java Springboot application that handles all likes and comments that users leave on the platform.

## Startup flow

1. Load `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` and `CORS_ALLOWED_ORIGINS` from environment variables
2. Run PostgreSQL migrations if they are available and necessary
3. Start the Springboot endpoint to listen for incoming HTTP requests from port `27087`

## Core components

- `SecurityConfig`
    - Configures CORS to allow requests from the origin marked in the ci/cd
- `Application.properties`
    - Configures environment variables into local variables
    - Defines the service endpoint
- `InteractionController`
  - Handles interaction requests
- `Comment`
  - Defines the database structure for comments
- `CustomLike`
  - Defines the database structure for likes

## Concurrency and runtime behavior

- The service leverages Java's multithreading capabilities and the Spring Boot framework to handle concurrent requests efficiently.
- Each incoming HTTP request is handled in a separate process, allowing for high throughput and responsiveness.

## Security considerations

- The service validates and sanitizes all incoming data to prevent injection attacks and ensure data integrity.
- It uses CORS configuration to restrict access to trusted origins, reducing the risk of cross-origin attacks.
- The service relies on Java Springboot built-in security features, such as CSRF protection and secure session management, to safeguard against common web vulnerabilities.
