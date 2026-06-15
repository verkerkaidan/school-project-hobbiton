---
sidebar_position: 1
---

# Recognition Service

The **Recognition Service** is a Java Springboot service.

## Overview

The service performs image analysis and auto-tagging of uploaded media. Using an ai-model that is downloaded at docker startup.

## Port

| Container port | Host port |
| -------------- | --------- |
| `27088`        | `27088`   |

## Responsibilities

- Analyze images to detect cars
- Return a ContentFlag to assign to the image
- Integrated with Media Service pipelines

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
