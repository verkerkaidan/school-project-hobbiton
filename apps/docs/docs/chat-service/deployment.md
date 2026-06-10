---
sidebar_position: 6
---

# Deployment & Configuration

## Environment variables

- `CHAT_DB_CONNECTION_STRING`
  - required
  - connection string used by `Npgsql` to connect to PostgreSQL

## Docker Compose

The service is defined in the repository `docker-compose.yml` as `chat-service`.

- Container name: `chat-service`
- Host port: `29294`
- Container port: `29294`

### Example environment snippet

```yaml
chat-service:
  build:
    context: ./apps/chat-service
  container_name: chat-service
  ports:
    - "29294:29294"
  environment:
    - CHAT_DB_CONNECTION_STRING=${CHAT_DB_CONNECTION_STRING}
```

## Build and run locally

From `apps/chat-service`:

```bash
dotnet build
dotnet run
```

The service starts on the port configured by ASP.NET Core defaults and is exposed as `29294` in the repository's Docker setup.

## Runtime behavior

- The service will fail to start if `CHAT_DB_CONNECTION_STRING` is missing.
- Database migrations run before the web host starts.
- CORS is configured to allow local development origins and the API gateway host.

## Notes

- The chat-service does not expose its own HTTP authentication layer; it expects upstream services or clients to handle authentication and to pass valid user identifiers.
- The application uses .NET 10 and depends on packages: `Google.Protobuf`, `Grpc.Tools`, `Microsoft.AspNetCore.OpenApi`, and `Npgsql`.
