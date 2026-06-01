
# BPSEE Proof of Concept — 10 weken

Dit repository bevat het Proof of Concept (POC) project voor BPSEE, bedoeld voor de komende 10 weken. Doel: 'snel' een werkende end-to-end demo opleveren met een moderne frontend en een Java/Spring Boot backend in een monorepo-structuur.

Team

- Dagmar
- Demi
- Tygo
- Matthijs
- Aidan

Scope

- Frontend: React + Next.js + TypeScript
- Backend: Spring Boot (Java)
- Doel: POC met feature(s) die frontend en backend koppelen

Monorepo-structuur (aanbevolen)

- /apps/hobby-frontend — Next.js app (React, TypeScript)
- /apps/feed-service — Spring Boot service
- /apps/media-service — Spring Boot service
- /apps/post-service — Spring Boot service
- /apps/profile-connection-service — Spring Boot service

Werkwijze & workflow

- Branching: werk met korte feature-branches op basis van `main`.
- Pull Requests: verplicht code review (minimaal één reviewer).
- Commits: schrijf duidelijke commit messages; één feature per branch.
- Code quality: configureer en gebruik ESLint/Prettier voor frontend en een formatter/checkstyle voor backend.

Monorepo tooling (suggesties)

- Package management: 'npm' of 'pnpm'

CI / CD (kort)

- TODO: Pipeline via GitHub actions om te deployen

Testing

- Frontend: unit tests met Vitest/Jest/React Testing Library;
- Backend: unittesting met JUnit + Spring Test.

Deployment (POC)

- Voor de POC is een eenvoudige deployment naar de RON SERVER voldoende.
- Monorepo deployment met Docker zodat we de hele applicatie met 1 command kunnen runnen.

Gitignore

- Elk project moet zijn eigen gitignore hebben om de folders lean te houden en verwarrende changes te vermijden.

Contact

Neem contact op met het team (Dagmar, Demi, Tygo, Matthijs, Aidan) voor vragen of taken.

## Lokale development
Per microservice zal de database moeten draaien, dit is 1 globale database. Om lokaal te developen run in de rootfolder het volgende commando voor het aanzetten van *enkel* de database:
```powershell
docker compose up -d database
```
Voor het runnen van elke microservice *die is opgenomen in de docker-compose.yml* run het volgende commando:
```powershell
docker compose up -d
```
 
## Redis (caching)
We added a Redis service to the compose files for caching. The Redis configuration file lives at `./docker/redis/redis.conf` and is mounted into the containers.

To start only Redis locally:
```powershell
docker compose up -d redis
```

To start Redis together with all services:
```powershell
docker compose up -d
```

Notes:
- The compose files expose Redis on port 6379 for convenience in local development. In production you can remove the port mapping if you don't want Redis reachable from the host.
- For production passwords/secrets, do not commit credentials to git. Instead set a strong password and enable `requirepass` in the redis.conf or inject via a secrets manager.
## Microservice opnemen in docker-compose.yml
1. Zet een `dockerfile` op in lokale microservice folder structuur. Voor java is dit de algemene Dockerfile setup, enkel de **{snapshot_name}** is per project verschillend. Voor talen buiten JAVA is de dockerfile even zelf uitzoeken.
```dockerfile
FROM java:21  
FROM maven:sapmachine  
  
WORKDIR /app  
  
COPY . /app  
  
ADD pom.xml /app  
RUN mvn verify clean --fail-never  
  
RUN mvn clean package -DskipTests  
  
ENTRYPOINT ["java", "-jar", "/app/target/{snapshot_name}.jar"]
```
2. Test of deze docker build werkt, run volgende commando's, als deze blijven 	````
```dockerfile
docker build -t test .
docker run test
```
3. Voeg *.env* variables toe aan de algemene *.env* (deze pushen we **NOOIT** naar GIT). Update ook de *.env.example*.  
Voor de database url geldt dat de microservice-specifieke *.env* op localhost moet staan, maar de algemene *.env* op database. Dit is een dingetje van docker, dat die de url anders niet kan vinden/koppelen.
4. Voeg aan de *docker-compose.yml* het volgende toe: *voor port_id is het 80{8+current count}*
```dockerfile
  {service_name}:
    build: ./apps/post-service
    container_name: post-service
    ports:
      - "{port}:{port}"
    environment:
      DB_HOST: database
      DB_PORT: 5432
      SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/${.ENV_VARIABLE_DATABASE}
      SPRING_DATASOURCE_USERNAME: ${.ENV_VARIABLE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${.ENV_VARIABLE_PASSWORD}
    cpus: 2
    mem_limit: 2048m
    networks:
      - microservices-network
    depends_on:
      database:
        condition: service_healthy
```
Om nu te testen of updated docker-compose werkt, run het volgende commando:
```dockerfile
docker compose up -d
```

Bij problemen met docker, ping Tygo Houweling