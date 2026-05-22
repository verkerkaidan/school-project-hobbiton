---
sidebar_position: 1
---

# Hobby Frontend

The **Hobby Frontend** is the Next.js web application that users interact with directly.

## Port

| Container port | Host port |
| -------------- | --------- |
| `19293`        | `19293`   |

## Sections

| Route             | Description               |
| ----------------- | ------------------------- |
| `/`               | Home / discover feed      |
| `/authentication` | Login and registration    |
| `/chats`          | Real-time chat            |
| `/discover`       | Explore users and hobbies |
| `/notifications`  | User notifications        |
| `/posts`          | Post creation and detail  |
| `/profile`        | User profile              |
| `/saved`          | Saved items               |
| `/settings`       | Account settings          |

## Build Arguments

| Argument                                | Description                 |
| --------------------------------------- | --------------------------- |
| `NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE` | API Gateway URL for SSR     |
| `NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE` | API Gateway URL for browser |
| `NEXT_PUBLIC_AUTHENTICATION_URL`        | Authentication Service URL  |
| `NEXT_PUBLIC_LIBRARY_SERVICE_URL`       | Library Service URL         |
| `NEXT_PUBLIC_MEDIA_URL`                 | Media Service URL           |
| `NEXT_PUBLIC_INTERACTION_URL`           | Interaction Service URL     |
| `NEXT_PUBLIC_WS_CHAT_SERVICE_URL`       | Chat WebSocket URL          |
| `NEXT_PUBLIC_CHAT_SERVICE_URL`          | Chat HTTP URL               |
| `NEXT_PUBLIC_NOTIFICATION_URL`          | Notification Service URL    |

## Technology Stack

- **Language:** TypeScript
- **Framework:** Next.js (App Router)
- **Styling:** Tailwind CSS
