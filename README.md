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

Environment & poorten

- Frontend: standaard Next.js dev-server op poort 3000
- Backend: standaard Spring Boot op poort 8080
- Gebruik `.env`- of `application.yml`-bestanden voor configuratie en API-URL's.

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

Communicatie & meetings

- Plan wekelijkse check-ins en korte demos every sprint (kortere sprints tijdens de 10 weken).

Gitignore

- Elk project moet zijn eigen gitignore hebben om de folders lean te houden en verwarrende changes te vermijden.

Contact

Neem contact op met het team (Dagmar, Demi, Tygo, Matthijs, Aidan) voor vragen of taken.
