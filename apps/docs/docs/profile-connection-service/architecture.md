---
sidebar_position: 2
---

# Profile Connection Service Architecture

## Startup Flow

1. Spring Boot loads configuration from `application.properties` and environment variables.
2. The app connects to PostgreSQL through `SPRING_DATASOURCE_URL`.
3. Hibernate updates the schema with `spring.jpa.hibernate.ddl-auto=update`.
4. `DatabaseSeeder` inserts mock profiles, follows, blocks, and reports when the `profiles` table is empty.
5. Spring Security disables CSRF, permits requests, and applies CORS rules from `CORS_ALLOWED_ORIGINS`.
6. Controllers are exposed under the `/api` servlet context path.

## Core Components

- `FollowController` / `FollowService`
  - toggles follow relationships and returns followers/following lists.
- `BlockController` / `BlockService`
  - toggles block relationships, removes follows between blocked users, and returns blocked profile IDs.
- `ReportController` / `ReportService`
  - creates profile reports, fetches by target/initiator/status, updates status, and deletes report data.
- `AuditLogService`
  - writes audit log records for right-to-be-forgotten deletion operations.
- `DatabaseSeeder`
  - seeds local/demo profile graph data only when no profiles exist.
- Spring Boot Actuator
  - exposes `/actuator/health` and `/actuator/prometheus` endpoints for liveness checks and Prometheus metrics scraping.

## Relationship Rules

- A profile cannot follow itself.
- A profile cannot block itself.
- Creating a block removes follow records in both directions between the blocker and blocked profile.
- Profile reports cannot target the same profile as the initiator.
- Follow, block, and report deletion endpoints remove records where the user is either initiator or target.

## Sequence Diagrams

### Toggle Follow

```mermaid
sequenceDiagram
  participant Client
  participant Gateway as API Gateway
  participant Service as Profile Connection Service
  participant DB as PostgreSQL

  Client->>Gateway: PUT /api/follows/{targetProfileId}
  Gateway->>Service: Forward X-Profile-Id header
  Service->>Service: Reject self-follow
  Service->>DB: Look up existing follower/following row
  alt Relationship exists
    Service->>DB: Delete follow row
    Service-->>Gateway: false
  else Relationship missing
    Service->>DB: Insert follow row
    Service-->>Gateway: true
  end
  Gateway-->>Client: Boolean follow state
```

### Toggle Block

```mermaid
sequenceDiagram
  participant Client
  participant Service as Profile Connection Service
  participant DB as PostgreSQL

  Client->>Service: PUT /api/blocks/{targetProfileId}
  Service->>Service: Reject self-block
  Service->>DB: Look up existing blocker/blocked row
  alt Block exists
    Service->>DB: Delete block row
    Service-->>Client: false
  else Block missing
    Service->>DB: Delete follow rows in both directions
    Service->>DB: Insert block row
    Service-->>Client: true
  end
```

### Delete User Relationship Data

```mermaid
sequenceDiagram
  participant Admin as Admin or deletion workflow
  participant Service as Profile Connection Service
  participant DB as PostgreSQL

  Admin->>Service: DELETE /api/follows/user/{userUUID}
  Service->>DB: Delete rows where user is follower
  Service->>DB: Delete rows where user is following target
  Service->>DB: Insert audit_log_profile_connections row
  Service-->>Admin: deletedCount, auditLogId, timestamp
```
