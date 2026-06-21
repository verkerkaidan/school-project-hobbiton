---
sidebar_position: 0
slug: /
---

# Introduction

Hobbithon is a hobby-sharing social platform built as a polyglot microservices system. Users can discover others with shared interests, create and react to posts, chat in real-time, and browse a media library — all served by a fleet of independently deployable services written in Java, TypeScript, C#, Rust, Elixir, and Go.

This documentation covers the architecture, API specifications, database schemas, and deployment guides for every service in the platform.

## Table of Contents

### Services

| Service | Description |
|---|---|
| [API Gateway](api-gateway/index.md) | Spring Boot gateway — single entry point for all client requests |
| [Authentication Service](authentication-service-c/index.md) | Handles user authentication and token management |
| [Hobby Frontend](hobby-frontend/index.md) | The client-facing frontend application |
| [Post Service](post-service/index.md) | Create, read, and manage user posts |
| [Profile Service](profile-service/index.md) | User profile management |
| [Notification Service](notification-service/index.md) | Push and in-app notifications |
| [Bug Report Service](bug-report-service/index.md) | User-submitted bug reports |
| [Profile Connection Service](profile-connection-service/index.md) | Follow and connection management between users |
| [Chat Service](chat-service/index.md) | Real-time messaging via WebSockets |
| [Media Service](media-service-elx/index.md) | Media upload and storage |
| [Interaction Service](interaction-service/index.md) | Likes, reactions, and other post interactions |
| [Recognition Service](recognition-service/index.md) | Content recognition and classification |
| [Library Service](library-service/index.md) | Browsable media library |

### Infrastructure

| Component | Description |
|---|---|
| [Docker](docker/index.md) | Container configuration and orchestration |
| [Redis](redis/index.md) | Caching and pub/sub infrastructure |
| [CI/CD](ci-cd/index.md) | Continuous integration and deployment pipelines |
