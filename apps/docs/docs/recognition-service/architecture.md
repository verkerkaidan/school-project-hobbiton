---
sidebar_position: 2
---

# Recognition Service Architecture

The Recognition Service is a basic Java Springboot application that provides ContentFlags for uploaded files. This 
service makes use of an ai-model and has no database set up.

## Startup flow

1. Download the PyTorch model from `MODEL_URL` into `/app/models/car_classifier.pt` in `entrypoint.sh`
2. Load `CORS_ALLOWED_ORIGINS` and Spring configuration from `application.properties`
3. Initialize DJL with the PyTorch engine and load the model from `ai.model.path`
4. Start the Spring Boot endpoint on port `27088` with the `/api` context path

## Core components

- `SecurityConfig`
  - Configures CORS to allow requests from the origin marked in the ci/cd
- `Application.properties`
  - Configures file size limits
  - Configures environment variables into local variables
  - Defines the `/api` context path and actuator endpoints
- `RecognitionController`
  - Handles recognition requests
- `RecognitionService`
  - Loads the DJL model and maps model labels to `SAFE` or `RISK`

## Concurrency and runtime behavior

- Spring handles concurrent HTTP requests on request threads.
- Within a single recognition request, the controller evaluates the submitted files with `CompletableFuture.supplyAsync` and returns after all futures complete.

## Security considerations

- The service disables CSRF and permits all requests in its own Spring Security configuration because it is intended to sit behind the gateway/internal network.
- CORS is restricted to `CORS_ALLOWED_ORIGINS`.
- Image bytes are validated with `ImageIO` before inference, but the service does not validate user identity or roles.
