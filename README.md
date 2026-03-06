
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

- /apps/frontend — Next.js app (React, TypeScript)
- /apps/backend — Spring Boot service
- /apps/services — losse microservices (elk service in eigen submap)

Werkwijze & workflow

- Branching: werk met korte feature-branches op basis van `main`.
- Pull Requests: verplicht code review (minimaal één reviewer).
- Commits: schrijf duidelijke commit messages; één feature per branch.
- Code quality: configureer en gebruik ESLint/Prettier voor frontend en formatter/checkstyle voor backend.

Monorepo tooling (suggesties)

- Package management: 'npm' of 'pnpm'

CI / CD (kort)

- Pipeline via GitHub actions om te deployen?

Conventies
  
-  

Testing

- Frontend: unit tests met Vitest/Jest/React Testing Library;
- Backend: unit- en integratietests met JUnit + Spring Test.

Deployment (POC)

- Voor de POC is een eenvoudige deployment naar één staging omgeving voldoende.
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
  build: ./{target_folder}
  container_name: {service_name}
  ports:  
    - '{port_id}:{port_id}'  
  env_file:  
    - .env  
  cpus: 2  
  mem_limit: 2048m  
  networks:  
    - microservices-network  
  depends_on:  
    - database
```
Om nu te testen of updated docker-compose werkt, run het volgende commando:
```dockerfile
docker compose up -d
```

Bij problemen met docker, ping Tygo Houweling