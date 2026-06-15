---
sidebar_position: 2
---

# Recognition Service Architecture

The Recognition Service is a basic Java Springboot application that provides ContentFlags for uploaded files. This 
service makes use of an ai-model and has no database set up.

## Startup flow

1. Load `RECOGNITION_MODEL_NAME` and `CORS_ALLOWED_ORIGINS` from environment variables
2. Start the Springboot endpoint to listen for incoming HTTP requests from port `27088`

## Core components

- `SecurityConfig`
  - Configures CORS to allow requests from the origin marked in the ci/cd
- `Application.properties`
  - Configures file size limits
  - Configures environment variables into local variables
  - Defines the service endpoint
- `RecognitionController`
  - Handles recognition requests

## Concurrency and runtime behavior

- The service leverages Java's multithreading capabilities and the Spring Boot framework to handle concurrent requests efficiently.
- Each incoming HTTP request is handled in a separate process, allowing for high throughput and responsiveness.

## Security considerations

- The service validates and sanitizes all incoming data to prevent injection attacks and ensure data integrity.
- It uses CORS configuration to restrict access to trusted origins, reducing the risk of cross-origin attacks.
- The service relies on Java Springboot built-in security features, such as CSRF protection and secure session management, to safeguard against common web vulnerabilities.
