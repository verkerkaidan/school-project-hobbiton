# Interaction Service

Java Spring Boot service for likes and comments on posts and comments.

## Run

From the repository root:

```bash
docker compose up -d --build interaction-service
```

The service listens on `http://localhost:27087` with the Spring context path `/api`.

## Endpoints

Endpoints are defined in `src/main/java/com/interactionservice/controller/InteractionController.java`.

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/interaction/{userId}/{referenceId}` | Check whether a user liked a reference |
| `GET` | `/api/interaction/{referenceId}` | Get like count and comments for a reference |
| `GET` | `/api/interaction/likes/{referenceId}` | Get user ids that liked a reference |
| `POST` | `/api/interaction` | Create a comment |
| `POST` | `/api/interaction/batch` | Bulk fetch interactions for references |
| `POST` | `/api/interaction/likes/batch?userId={userId}` | Bulk check liked references for one user |
| `PUT` | `/api/interaction/{userId}/{referenceId}` | Toggle a like |
| `DELETE` | `/api/interaction/{referenceId}` | Delete a comment/reference |
| `DELETE` | `/api/interaction/post` | Delete interactions for post references |
| `DELETE` | `/api/interaction/user/{userId}` | Delete all likes and comments for a user |

## Configuration

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `CORS_ALLOWED_ORIGINS`

Hibernate updates the PostgreSQL schema at startup with `spring.jpa.hibernate.ddl-auto=update`.

## Testing

From `apps/interaction-service`:

```bash
./mvnw test
```
