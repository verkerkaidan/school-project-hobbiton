---
sidebar_position: 1
---

# Profile Connection Service

The **Profile Connection Service** manages profile-to-profile relationships for follows, blocks, user reports, and compliance audit logs.

## Responsibilities

- Toggle follow and unfollow relationships
- Query followers and following lists
- Toggle block and unblock relationships
- Remove follow relationships when a block is created
- Query profiles blocked by a user
- Create, fetch, update, and delete profile reports
- Delete all relationship/report records for a user and write audit log entries

## Port

| Runtime | Port | Notes |
| --- | --- | --- |
| Service port | `29574` | Set by `server.port` in `application.properties`. |
| Docker container port | `29574` | Exposed by `profile-connection-service`. |
| Docker host port | `29574` | Mapped as `29574:29574`. |
| PostgreSQL port | `5432` | The service connects to the shared `database` container. |

The API Gateway reaches this service through `PROFILE_CONNECTION_SERVICE_URL`.

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
- **Persistence:** Spring Data JPA / Hibernate
