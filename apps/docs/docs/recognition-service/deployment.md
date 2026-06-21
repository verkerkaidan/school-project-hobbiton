---
sidebar_position: 4
---

# Deployment & Configuration

## Environment variables
- `MODEL_URL`
  - URL for the PyTorch model artifact downloaded by `entrypoint.sh` into `/app/models/car_classifier.pt`
  - GitLab `/blob/` URLs are rewritten to `/raw/` before download
- `CORS_ALLOWED_ORIGINS`
    - String containing all origins that are allowed for CORS

If `MODEL_URL` is missing, `entrypoint.sh` logs an error and falls back to the hardcoded model artifact URL.
The Spring property `ai.model.path` points DJL at `/app/models`.

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
      MODEL_URL: ${RECOGNITION_MODEL_URL}
      CORS_ALLOWED_ORIGINS: ${MIDDLEWARE_ALLOWED_ORIGINS}
    ports:
      - "27088:27088"
```
