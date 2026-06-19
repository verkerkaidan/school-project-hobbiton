---
sidebar_position: 2
---

# Architecture

The Post Service follows a layered Spring Boot architecture with single-table inheritance for polymorphic post types.

## Layers

```
Controller → Service → DAO → JPA Repository → PostgreSQL
```

### Controller (`PostController`)

`@RestController @RequestMapping("/posts")` — handles all REST endpoints. Maps incoming DTOs to entities and delegates to the DAO.

### Service (`PostService`)

Contains one business logic method: `saveAsSetMediaInstanceFromSetUUID` — synchronises a set of `MediaInstance` entities from incoming UUIDs (adds new, removes stale).

### Data Access (`PostDAO`)

Facade over five JPA repositories (`PostRepository`, `GymRepository`, `ReadingRepository`, `OtherRepository`, `MediaInstanceRepository`). Provides unified access for CRUD, paginated queries, and hobby-type filtering.

### Model Layer (JPA Entities)

Uses `InheritanceType.JOINED`:

```
Post (abstract)
├── Gym
│   └── GymExercise (@OneToMany)
├── Reading
└── Other
```

### Configuration

- **SecurityConfig**: Disables CSRF, configures CORS from `cors.allowed-origins`, permits all requests (auth is handled by the API Gateway).
- **GlobalExceptionHandler**: `@RestControllerAdvice` mapping `HttpMessageNotReadableException` → 400, `RuntimeException` → 500.

## DTO Layer

Jackson polymorphic deserialisation via `@JsonTypeInfo`:

```
PostDTO (abstract)
├── GymDTO
├── ReadingDTO
└── OtherDTO
```

Clients send `"type": "GYM"` / `"READING"` / `"OTHER"` in JSON bodies.
