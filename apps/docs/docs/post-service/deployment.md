---
sidebar_position: 6
---

# Deployment & Configuration

## Environment Variables

| Variable                     | Required | Description                          |
|-----------------------------|----------|--------------------------------------|
| `SPRING_DATASOURCE_URL`     | Yes      | JDBC URL to PostgreSQL `posts_db`   |
| `SPRING_DATASOURCE_USERNAME`| Yes      | DB username                          |
| `SPRING_DATASOURCE_PASSWORD`| Yes      | DB password                          |
| `CORS_ALLOWED_ORIGINS`      | No       | Comma-separated origins (default: `http://localhost:23738`) |

## Docker Compose

```yaml
post-service:
  build:
    context: ./apps/post-service
  container_name: post-service
  restart: always
  ports:
    - "29293:29293"
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/${SPRING_DATASOURCE_POSTS_DATABASE}
    SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME_POSTS}
    SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD_POSTS}
    CORS_ALLOWED_ORIGINS: ${MIDDLEWARE_ALLOWED_ORIGINS}
  cpus: 0.5
  mem_limit: 512m
  depends_on:
    database:
      condition: service_healthy
```

## Build and run locally

From `apps/post-service`:

```bash
./mvnw spring-boot:run     # or `npm run dev`
```

Build a JAR:

```bash
./mvnw clean package -DskipTests   # or `npm run build`
```

## Docker Build

Multi-stage Dockerfile:

1. **Build stage** — `maven:3.9-eclipse-temurin-21`, runs `mvn clean package -DskipTests`
2. **Runtime stage** — `eclipse-temurin:21-jre`, runs `java -jar app.jar`

## Resource limits

| Environment | CPUs | Memory |
|-------------|------|--------|
| Local       | 0.5  | 512m   |
| Production  | 1    | 1024m  |
