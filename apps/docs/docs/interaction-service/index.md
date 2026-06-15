---
sidebar_position: 1
---

# Interaction Service

The **Interaction Service** is a Java Springboot service

## Overview

The service manages user interactions such as likes, comments, and reactions on posts.

## Port

| Container port | Host port |
| -------------- | --------- |
| `27087`        | `27087`   |

## Responsibilities

- Record and retrieve likes / reactions
- Manage comments on posts
- Expose interaction counts to the middleware

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build tool:** Maven (`mvnw`)
- **Database:** PostgreSQL
