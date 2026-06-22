---
sidebar_position: 6
---

# Deployment

## Docker Build

The Dockerfile uses a multi-stage build:

1. **Builder stage** (`node:20-alpine`) — installs dependencies with `npm ci`, then runs `npm run build`.
2. **Production stage** (`node:20-alpine`) — copies `.next`, `public`, `package*.json`, `node_modules`, and `next.config.ts`. Runs `npm start`.

All `NEXT_PUBLIC_*` environment variables are injected as build arguments because Next.js inlines them at build time.

## Docker Compose

| Setting | Value |
| --- | --- |
| Build context | `./apps/hobby-frontend` |
| Container name | `hobby-frontend` |
| Port mapping | `19293:19293` |
| Network | `microservices-network` |

## Environment Variables

| Variable | Required | Description |
| --- | --- | --- |
| `NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE` | Yes | API Gateway URL used during SSR |
| `NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE` | Yes | API Gateway URL used in the browser |
| `NEXT_PUBLIC_AUTHENTICATION_URL` | Yes | Authentication Service base URL |
| `NEXT_PUBLIC_LIBRARY_SERVICE_URL` | Yes | Library Service base URL |
| `NEXT_PUBLIC_MEDIA_URL` | Yes | Media Service base URL |
| `NEXT_PUBLIC_INTERACTION_URL` | Yes | Interaction Service base URL |
| `NEXT_PUBLIC_WS_CHAT_SERVICE_URL` | Yes | Chat WebSocket URL |
| `NEXT_PUBLIC_CHAT_SERVICE_URL` | Yes | Chat REST URL |
| `NEXT_PUBLIC_NOTIFICATION_URL` | Yes | Notification Service base URL |
| `NEXT_PUBLIC_APP_VERSION` | No | Version label displayed in the app |

## Local Development

```bash
cd apps/hobby-frontend
npm install
npm run dev
```

Create a `.env.local` with the variables above pointing to locally running services (or Docker containers).

## Runtime Notes

- All `NEXT_PUBLIC_*` variables are baked into the JavaScript bundle at build time — changing them requires a rebuild.
- The production server listens on `0.0.0.0:19293`.
- The app depends on the API Gateway, Authentication Service, and at minimum the Post Service to function. Chat and Notification features degrade gracefully without their respective services.
