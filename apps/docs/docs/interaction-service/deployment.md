---
sidebar_position: 5
---

# Deployment & Configuration

## Environment variables

- `SPRING_DATASOURCE_URL`
  - String used to connect to the database
- `SPRING_DATASOURCE_USERNAME`
  - String used to get rights into the interaction_database
- `SPRING_DATASOURCE_PASSWORD`
  - String used to get rights into the interaction_database
- `CORS_ALLOWED_ORIGINS`
    - String containing all origins that are allowed for CORS

## Docker Compose

The service is defined in the repository `docker-compose.yml` as `interaction-service`.

- Container name: `interaction-service`
- Host port: `27087`
- Container port: `27087`

### Example environment snippet

```yaml
interaction-service:
    build:
      context: ./apps/interaction-service
    container_name: interaction-service
    ports:
      - "27087:27087"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/${SPRING_DATASOURCE_INTERACTION_DATABASE}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME_INTERACTION}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD_INTERACTION}
      CORS_ALLOWED_ORIGINS: ${MIDDLEWARE_ALLOWED_ORIGINS}
```
