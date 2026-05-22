---
sidebar_position: 1
---

# Recognition Service

The **Recognition Service** performs image analysis and auto-tagging of uploaded media.

## Port

| Container port | Host port |
| -------------- | --------- |
| `27088`        | `27088`   |

## Responsibilities

- Analyse images to detect objects, scenes, and faces
- Return structured tags and confidence scores
- Integrate with the Media Service pipeline

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
