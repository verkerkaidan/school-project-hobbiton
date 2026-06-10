---
sidebar_position: 1
---

# Chat Service

The **Chat Service** is a .NET 10 microservice that handles real-time chat delivery, chat membership, and unread notification state.

## Overview

The service serves as the chat backend for Hobbithon. It provides:

- persistent chat rooms and messages
- WebSocket-based real-time chat delivery
- unread count notifications over a dedicated WebSocket channel
- chat membership and title management

## Quick links

- [Architecture](./architecture)
- [HTTP API](./http-api)
- [WebSocket API](./websocket-api)
- [Database Schema](./database-schema)
- [Deployment & Configuration](./deployment)

## Port

| Container port | Host port |
| -------------- | --------- |
| `29294`        | `29294`   |

## Responsibilities

- Persist and retrieve chat messages
- WebSocket real-time delivery
- Chat room metadata and membership management
- Unread message notification updates

## Configuration

The service requires `CHAT_DB_CONNECTION_STRING` to connect to PostgreSQL. The database connection string should be injected via environment variables.

## Technology Stack

- **Language:** C#
- **Framework:** ASP.NET Core (.NET 10)
- **Build tool:** `dotnet` / MSBuild
- **Database:** PostgreSQL
- **Packages:** `Npgsql`, `Microsoft.AspNetCore.OpenApi`, `Google.Protobuf`, `Grpc.Tools`
