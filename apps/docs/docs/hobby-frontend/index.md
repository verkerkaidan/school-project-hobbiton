---
sidebar_position: 1
---

# Hobby Frontend

The **Hobby Frontend** is the Next.js web application that users interact with directly. It provides a social-media-style experience centered around hobbies — users can create posts, follow each other, chat in real time, and track gym workouts, reading progress, and more.

## Port

| Container port | Host port |
| -------------- | --------- |
| `19293`        | `19293`   |

## Routes

| Route                    | Description                          |
| ------------------------ | ------------------------------------ |
| `/`                      | Home feed (posts from followed users)|
| `/authentication`        | Login and registration               |
| `/chats`                 | Chat list with unread badges         |
| `/chats/[chatId]`        | Individual real-time chat room       |
| `/discover`              | Search users, books, movies, exercises |
| `/notifications`         | Paginated notification list          |
| `/posts/[id]`            | Post detail view                     |
| `/posts/[id]/edit`       | Edit own post                        |
| `/profile`               | Current user's profile               |
| `/profile/[username]`    | Other user's profile                 |
| `/saved`                 | Reading lists (books, movies, exercises) |
| `/settings`              | Account, notifications, security     |
| `/admin`                 | Admin panel (admin-only)             |

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
| `NEXT_PUBLIC_APP_VERSION`               | App version label           |

## Technology Stack

| Category          | Technology                          |
| ----------------- | ----------------------------------- |
| Language          | TypeScript                          |
| Framework         | Next.js 16 (App Router)             |
| UI Library        | React 19                            |
| Styling           | Tailwind CSS v4                     |
| Animations        | Framer Motion                       |
| Icons             | Lucide React                        |
| Server State      | TanStack React Query                |
| HTTP Client       | Fetch API + Axios (chat)            |
| Real-time         | WebSocket (native)                  |
| Binary Protocol   | Protobuf.js (chat messages)         |
| Layout            | react-masonry-css (feed grid)       |
| Cookies           | js-cookie                           |

## Sections

- [Architecture](./architecture) — project structure, data flow, and design patterns
- [Components](./components) — UI component inventory
- [State Management](./state-management) — hooks, providers, and data fetching
- [API Integration](./api-integration) — how the frontend talks to backend services
- [Deployment](./deployment) — Docker build and environment variables
