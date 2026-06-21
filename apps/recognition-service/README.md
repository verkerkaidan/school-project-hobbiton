# Recognition Service

Java Spring Boot service that classifies uploaded image bytes as `SAFE` or `RISK` with a DJL/PyTorch model.

## Run

From the repository root:

```bash
docker compose up -d --build recognition-service
```

The service listens on `http://localhost:27088` with the Spring context path `/api`.

## Endpoint

| Method | Path | Body | Returns |
|--------|------|------|---------|
| `POST` | `/api/recognition` | `[{hash: string, file: base64-string}]` | `[{hash: string, contentFlag: "SAFE" \| "RISK", note: "AI driven change"}]` |

## Configuration

- `MODEL_URL`: model artifact URL downloaded by `entrypoint.sh` into `/app/models/car_classifier.pt`
- `CORS_ALLOWED_ORIGINS`: comma-separated CORS allowlist

If `MODEL_URL` is missing, the entrypoint falls back to the hardcoded model artifact URL.

## Testing

From `apps/recognition-service`:

```bash
./mvnw test
```
