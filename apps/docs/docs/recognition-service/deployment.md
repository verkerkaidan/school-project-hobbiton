---
sidebar_position: 4
---

# Deployment & Configuration

## Environment variables
- `MODEL_NAME`
  - String that references the name of a car classification model saved in an external project of 
    - Student at Hogeschool Leiden: [Matthijs Harteveld / s1147087](https://gitlab.inf-hsleiden.nl/s1147087/my-model-artifacts/-/tree/main/Hobbithon?ref_type=heads)
- `CORS_ALLOWED_ORIGINS`
    - String containing all origins that are allowed for CORS

Model name has a fallback in application.properties

## Docker Compose

The service is defined in the repository `docker-compose.yml` as `recognition-service`.

- Container name: `recognition-service`
- Host port: `27088`
- Container port: `27088`

### Example environment snippet

```yaml
recognition-service:
    build:
      context: ./apps/recognition-service
    container_name: recognition-service
    environment:
      MODEL_NAME: ${RECOGNITION_MODEL_NAME}
      CORS_ALLOWED_ORIGINS: ${MIDDLEWARE_ALLOWED_ORIGINS}
    ports:
      - "27088:27088"
```
