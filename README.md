
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

---

## Docker Compose (lokaal)

Het bestand `docker-compose.yml` is bedoeld voor lokale development. Het bouwt alle images lokaal vanuit de broncode in de `apps/` map.

### Services en poorten

| Service                    | Poort (host:container) | Beschrijving                          |
|---------------------------|------------------------|---------------------------------------|
| frontend                  | 19293:19293            | Next.js frontend                      |
| pgadmin                   | 8000:80                | PgAdmin webinterface voor de database |
| database                  | 5432:5432              | PostgreSQL database                   |
| post-service              | 29293:29293            | Beheert posts                         |
| profile-connection-service| 29574:29574            | Beheert connecties tussen profielen   |
| media-service             | 27086:27086            | Beheert media uploads                 |
| recognition-service       | 27088:27088            | Herkenningsservice                    |
| authentication-service    | 21525:21525            | Authenticatie en autorisatie          |
| profile-service           | 21526:21526            | Beheert gebruikersprofielen           |
| library-service           | 28877:28877            | OMDB-gebaseerde bibliotheekservice    |
| interaction-service       | 27087:27087            | Beheert interacties (likes, reacties) |
| middleware-service        | 23738:23738            | API gateway                           |

### Kenmerken
- Images worden **lokaal gebuild** vanuit de `apps/` map.
- Bevat PgAdmin voor database-beheer via de browser (`http://localhost:8000`).
- Elke service heeft `2 CPU` en `2048m` geheugen toegewezen.
- De database gebruikt een healthcheck; services starten pas als de database gezond is.
- Omgevingsvariabelen worden geladen vanuit een lokaal `.env` bestand (nooit committen).

---

## Docker Compose (productie)

Het bestand `docker-compose-prod.yml` wordt gebruikt op de productieserver. In tegenstelling tot de lokale versie trekt het kant-en-klare Docker images uit de GitLab Container Registry, in plaats van deze lokaal te builden.

### Services en poorten

| Service                    | Poort (host:container) | Beschrijving                          |
|---------------------------|------------------------|---------------------------------------|
| frontend                  | 19293:19293            | Next.js frontend                      |
| database                  | 29496:5432             | PostgreSQL database                   |
| post-service              | 29293:29293            | Beheert posts                         |
| profile-connection-service| 29574:29574            | Beheert connecties tussen profielen   |
| media-service             | 27086:27086            | Beheert media uploads                 |
| recognition-service       | 27088:27088            | Herkenningsservice                    |
| authentication-service    | 21525:21525            | Authenticatie en autorisatie          |
| profile-service           | 21526:21526            | Beheert gebruikersprofielen           |
| library-service           | 28877:28877            | OMDB-gebaseerde bibliotheekservice    |
| interaction-service       | 27087:27087            | Beheert interacties (likes, reacties) |
| middleware-service        | 23738:23738            | API gateway                           |

### Kenmerken
- Images worden **gepulld** vanuit de GitLab Container Registry (`$CI_REGISTRY_IMAGE/<service>:latest`).
- Geen PgAdmin in productie.
- Elke service heeft `1 CPU` en `1024m` geheugen toegewezen (lager dan lokaal).
- De database is bereikbaar op poort `29496` op de host (i.p.v. `5432` lokaal).
- Alle credentials en omgevingsvariabelen worden via de CI/CD pipeline als omgevingsvariabelen meegegeven.

---

## CI/CD Pipeline

De pipeline is geconfigureerd in `.gitlab-ci.yml` en bestaat uit drie stages: **test**, **build** en **deploy**.

### Stage 1 — test

Draait automatisch bij een **merge request** als er wijzigingen zijn in de betreffende service. Elke service heeft een eigen testjob:

| Job                              | Gewijzigde map                        |
|----------------------------------|---------------------------------------|
| `test:profile-connection-service`| `apps/profile-connection-service/**`  |
| `test:post-service`              | `apps/post-service/**`                |
| `test:media-service`             | `apps/media-service/**`               |
| `test:interaction-service`       | `apps/interaction-service/**`         |
| `test:library-service`           | `apps/library-service/**`             |
| `test:recognition-service`       | `apps/recognition-service/**`         |

Elk testjob:
- Gebruikt `maven:3.9-eclipse-temurin-21` als image.
- Voert `mvn clean test` uit.
- Publiceert JUnit-rapporten en Jacoco coverage als artifacts (30 dagen bewaard).

### Stage 2 — build

Draait alleen op de **`main` branch**. Bouwt Docker images voor alle services en pusht ze naar de GitLab Container Registry:

- `apigateway`, `authentication-service`, `interaction-service`, `library-service`, `media-service`, `post-service`, `profile-connection-service`, `profile-service`, `recognition-service`
- `hobby-frontend` — wordt gebouwd met `NEXT_PUBLIC_*` build-args vanuit CI/CD variabelen.

Na elke push wordt het lokale image verwijderd om diskruimte te besparen.

### Stage 3 — deploy

Draait alleen op de **`main` branch**. Deployt naar de studentenserver via SSH:

1. Kopieert `docker-compose-prod.yml` en `init-db.sh` naar de server.
2. Logt in op de GitLab Container Registry op de server.
3. Exporteert alle benodigde omgevingsvariabelen (database credentials, JWT, OAuth, etc.).
4. Voert `docker compose pull` uit om de nieuwste images op te halen.
5. Stopt en verwijdert de oude `frontend` container afzonderlijk (zodat de nieuwe Next.js build direct actief is).
6. Start alle services opnieuw op met `docker compose up -d --remove-orphans`.

### Benodigde CI/CD variabelen

De volgende variabelen moeten zijn ingesteld in de GitLab project settings onder **Settings → CI/CD → Variables**:

| Variabele                                  | Gebruik                              |
|--------------------------------------------|--------------------------------------|
| `SSH_PRIVATE_KEY`                          | SSH-sleutel voor toegang tot de server|
| `POSTGRES_USER` / `POSTGRES_PASSWORD`      | Hoofd Postgres credentials           |
| `PGADMIN_DEFAULT_EMAIL` / `PGADMIN_DEFAULT_PASSWORD` | PgAdmin login               |
| `SPRING_DATASOURCE_*`                      | Per-service database credentials     |
| `JWT_ISSUER` / `JWT_SECRET`                | JWT configuratie                     |
| `OAUTH_CLIENT_ID` / `OAUTH_CLIENT_SECRET`  | OAuth configuratie                   |
| `ADMIN_TEST_USER` / `ADMIN_TEST_PASSWORD`  | Testgebruiker voor seeding           |
| `OMDB_API_KEY` / `OMDB_API_URL`            | OMDB API voor de library-service     |
| `NEXT_PUBLIC_*`                            | Frontend build-time variabelen       |
| `MEDIA_SERVICE_URL` / `POST_SERVICE_URL` / etc. | API gateway routering          |