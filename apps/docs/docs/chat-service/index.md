---
sidebar_position: 1
---

# Chat Service

The **Chat Service** provides real-time messaging between users.

## Port

| Container port | Host port |
| -------------- | --------- |
| `29294`        | `29294`   |

## Responsibilities

- Persist and retrieve chat messages
- WebSocket / SignalR real-time delivery
- Chat room / direct message management

## Configuration

Connection strings and settings are managed in `appsettings.json`. Sensitive values should be provided via environment variables at runtime.

## Technology Stack

- **Language:** C#
- **Framework:** ASP.NET Core (.NET)
- **Build tool:** `dotnet` / MSBuild
- **Database:** PostgreSQL
