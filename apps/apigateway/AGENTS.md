# apigateway

Spring Boot **4.0.5** (newest version in the monorepo), Spring Cloud 2025.1.1, Java 21, Maven.

Acts as the single entry point for the frontend. Responsibilities: request routing via OpenFeign, GraphQL API, RabbitMQ event publishing, and CORS.

## Key dependencies

- `spring-cloud-starter-openfeign` + `feign-okhttp` — declare a `@FeignClient` per downstream service.
- `spring-cloud-starter-loadbalancer` — client-side load balancing.
- `spring-boot-starter-graphql` — GraphQL schema in `src/main/resources/graphql/*.graphqls`.
- `spring-boot-starter-amqp` — RabbitMQ via `RabbitTemplate`.
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI at `/swagger-ui.html`.
- `spring-boot-starter-webflux` — reactive WebClient available alongside Feign.

## Package layout

```
src/main/java/com/apigateway/apigateway/
  ApigatewayApplication.java
  controllers/     — REST + GraphQL controllers
  services/        — orchestration logic
  clients/         — @FeignClient interfaces per downstream service
  config/          — CORS, Feign, RabbitMQ, Security config
  dto/             — request/response objects
  models/          — domain models (no JPA here; gateway doesn't own a DB schema)
```

## Key conventions

- **Routing**: Add a `@FeignClient` interface in `clients/` for each new downstream service. Set the URL via a `@Value("${service.url}")` property backed by an env var.
- **GraphQL**: Schema files go in `src/main/resources/graphql/`. Resolvers are `@Controller` classes annotated with `@QueryMapping` / `@MutationMapping`.
- **CORS**: Configured centrally in a `@Configuration` bean — do not add per-controller `@CrossOrigin`.
- **Auth forwarding**: JWT from the `Authorization` header is forwarded downstream by Feign request interceptors — do not re-validate tokens here unless the gateway is explicitly responsible for a route.
- **RabbitMQ**: Publish events with `RabbitTemplate.convertAndSend(exchange, routingKey, payload)`. Declare exchanges/queues in a `@Configuration` bean.

## Spring Boot 4 notes

- Spring Boot 4 requires Java 21+.
- Some Spring Security APIs changed from Boot 3 — prefer the lambda-DSL style (`http.authorizeHttpRequests(auth -> auth.requestMatchers(...).permitAll())`).
- `spring-boot-starter-webflux` is included alongside `spring-boot-starter-web`; be careful not to mix blocking and reactive code paths unintentionally.

## Building & testing

```bash
mvn clean test
mvn spring-boot:run
```
