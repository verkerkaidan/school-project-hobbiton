# chat-service

C# / .NET 10, ASP.NET Core, gRPC (Google.Protobuf + Grpc.Tools), WebSocket, JWT Bearer auth, PostgreSQL (Npgsql).

## Project structure

```
ChatService/
  Program.cs            — app entry point & DI setup
  Protos/               — .proto definition files (gRPC contracts)
  Services/             — gRPC service implementations
  Controllers/          — REST / WebSocket controllers
  Data/                 — DbContext + EF Core models (if used) or raw Npgsql
  Models/               — domain/DTO classes
ChatService.Tests/      — xUnit test project
```

## Key conventions

- **gRPC**: proto files live in `Protos/`. After editing a `.proto`, rebuild the project — `Grpc.Tools` generates C# stubs automatically. Keep proto definitions backwards-compatible (add fields, never remove or renumber).
- **Auth**: JWT Bearer tokens are validated by `Microsoft.AspNetCore.Authentication.JwtBearer`. The shared `JWT_SECRET` env var is used as the signing key. Do not bypass `[Authorize]` without explicit intent.
- **Database**: use Npgsql (raw SQL or Dapper) rather than Entity Framework unless EF is already in use in a file you're editing. Database name: `chat_db`.
- **WebSocket**: upgrade is handled in an ASP.NET middleware or controller action; follow the existing pattern for the WebSocket loop (receive → process → send).
- **Dependency injection**: register services in `Program.cs` via `builder.Services.Add*`. Prefer constructor injection.
- **Configuration**: read env vars via `builder.Configuration["KEY"]` or `IConfiguration`. Never hard-code connection strings or secrets.

## Building & testing

```bash
dotnet build
dotnet test                          # runs ChatService.Tests
dotnet run --project ChatService     # starts on port 29294
```

## Environment variables required

| Variable | Purpose |
|----------|---------|
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port |
| `DB_USER` | DB user |
| `DB_PASSWORD` | DB password |
| `JWT_SECRET` | Shared JWT signing secret |
