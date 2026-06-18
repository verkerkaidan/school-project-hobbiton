# post-service (and other Java/Spring Boot services)

Spring Boot 3.2.5, Java 21, Maven. These guidelines also apply to `profile-service`, `profile-connection-service`, and `interaction-service` unless their own AGENTS.md says otherwise.

## Package layout

```
src/main/java/com/<service>/<service>/
  <Service>Application.java   — entry point
  controllers/                — REST controllers (@RestController)
  services/                   — business logic (@Service)
  dao/                        — data access (Spring Data JPA repositories or @Repository)
  models/                     — JPA entities (@Entity)
  dto/                        — request/response POJOs
  config/                     — Spring configuration classes
src/test/java/…               — JUnit 5 tests
```

## Key conventions

- **Layered architecture**: Controller → Service → DAO/Repository. Never put business logic in a controller.
- **Database**: Spring Data JPA + PostgreSQL. Define entities in `models/`, repositories in `dao/`.
- **DTOs**: Use separate DTO classes for request bodies and response bodies — never expose JPA entities directly in API responses.
- **Security**: JWT validation via Spring Security filter chain. JWT secret loaded from `JWT_SECRET` env var.
- **Service-to-service calls**: Spring Cloud OpenFeign. Define a `@FeignClient` interface in `config/` or a dedicated `client/` package.
- **API docs**: springdoc-openapi is included. Annotate controllers with `@Operation` / `@Tag` where useful.
- **Configuration**: Use `application.properties` or `application.yml`. Sensitive values (DB credentials, JWT secret) come from environment variables — never hard-code them.

## Building & testing

```bash
# From service root
mvn clean test           # run unit tests
mvn clean package        # build JAR
mvn spring-boot:run      # run locally (requires DB)
```

Tests use JUnit 5 via `spring-boot-starter-test`. Prefer `@SpringBootTest` for integration tests and `@WebMvcTest` for controller-layer tests.

## Common pitfalls

- JPA `@OneToMany` / `@ManyToOne` relationships are lazy by default — always join-fetch or use `@EntityGraph` when you know you'll need the association.
- Enable `spring.jpa.open-in-view=false` to avoid accidental lazy loading in the web layer.
- Cross-service calls via Feign will fail at startup if the target service URL env var is missing — always document required env vars in `application.properties`.
