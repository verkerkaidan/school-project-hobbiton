---
sidebar_position: 2
---

# Pipeline Architecture

## Validate

A single job (`check:changelog`) runs on MRs targeting `main`. It verifies that `CHANGELOG.md` has been modified — if not, the pipeline fails with a reminder message.

```yaml
check:changelog:
  stage: validate
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"
          && $CI_MERGE_REQUEST_TARGET_BRANCH_NAME == "main"'
  script:
    - git fetch origin $CI_MERGE_REQUEST_TARGET_BRANCH_NAME
    - git diff --name-only origin/$CI_MERGE_REQUEST_TARGET_BRANCH_NAME...HEAD
      | grep -q "^CHANGELOG.md$"
```

## Test

Per-service test jobs run on MRs when the service's files change. Each job uses the language-appropriate toolchain.

| Job | Service | Language | Image | Command |
|-----|---------|----------|-------|---------|
| `test:profile-connection-service` | profile-connection-service | Java | `maven:3.9-eclipse-temurin-21` | `mvn clean test` |
| `test:post-service` | post-service | Java | `maven:3.9-eclipse-temurin-21` | `mvn clean test` |
| `test:interaction-service` | interaction-service | Java | `maven:3.9-eclipse-temurin-21` | `mvn clean test` |
| `test:recognition-service` | recognition-service | Java | `maven:3.9-eclipse-temurin-21` | `mvn clean test` |
| `test:chat-service` | chat-service | C# | `mcr.microsoft.com/dotnet/sdk:10.0` | `dotnet test` |
| `test:media-service` | media-service | Elixir | `elixir:1.19.5-otp-27-alpine` | `mix test` |
| `test:library-service` | library-service | Rust | `rust:1.96-slim` | `cargo test` |
| `test:notification-service` | notification-service | Go | `golang:1.23` | `go test ./...` |
| `test:bug-report-service` | bug-report-service | Go | `golang:1.23` | `go test ./...` |

Java Maven jobs produce JUnit XML reports and JaCoCo coverage reports as CI artifacts.

### External Services

Some test jobs require backing services:

- **media-service** (Elixir): depends on a `postgres:15-alpine` sidecar container for database tests.
- **library-service** (Rust): depends on a `redis:7-alpine` sidecar for Redis-backed tests.

### Not tested in CI

| Service | Reason |
|---------|--------|
| `authentication-service-c` | C service — no dedicated CI test job. Tests can be run manually via `make test` (requires PostgreSQL). |
| `apigateway` | E2E tests cover gateway routing — run via Playwright outside CI. |
| `hobby-frontend` | E2E tests cover frontend — run via Playwright outside CI. |
| `profile-service` | Tests exist in the repo but are not wired into CI. |
