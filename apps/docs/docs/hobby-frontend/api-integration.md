---
sidebar_position: 5
---

# API Integration

The frontend communicates with multiple backend microservices. Most traffic flows through the API Gateway; a few services are called directly.

## API Clients

All clients live in `/lib`. Each exports plain async functions — no classes.

### GraphQL — `graphQlClient.ts`

Used for read-heavy queries: posts, profiles, followers, reports.

- Detects server vs client context automatically.
- **Server-side** → uses `NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE`, reads token from `next/headers` cookies.
- **Client-side** → uses `NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE`, reads token from `js-cookie`.
- Sends `POST` to `/api/graphql` on the API Gateway.

### REST via API Gateway

Most write operations go through the gateway as REST calls:

| Client | Key endpoints | Description |
| --- | --- | --- |
| `postClient.ts` | `POST /api/create-post`, `PUT /api/posts/{id}`, `DELETE /api/posts/{id}` | Post CRUD |
| `profileClient.ts` | `PUT /api/profile/update`, `DELETE /api/delete-own-profile`, `GET /api/search-profiles` | Profile management |
| `interactionClient.ts` | `POST /api/interaction/like`, `DELETE /api/interaction/like`, `POST /api/interaction/comment` | Likes and comments |
| `libraryClient.ts` | `GET /api/library/search`, `GET/POST /api/library/users/{id}/lists` | Library search and reading lists |
| `adminClient.ts` | `GET /api/admin/users`, `DELETE /api/admin/posts/{id}`, `PUT /api/admin/reports/{id}` | Admin operations |
| `bugReportClient.ts` | `POST /api/bug-reports`, `GET /api/admin/bug-reports` | Bug report CRUD |
| `reportClient.ts` | `POST /api/media/report`, `PUT /api/media/content-flag` | Content reporting |
| `registerClient.ts` | `POST /api/register` | User registration |

### Authentication Service — `authenticationClient.ts`

Called directly (not through the gateway):

| Endpoint | Method | Description |
| --- | --- | --- |
| `/api/auth/login` | POST | Returns token + userId |
| `/api/auth/register` | POST | Create account |
| `/api/auth/email` | PUT | Update email |
| `/api/auth/password` | PUT | Change password |

### Chat Service — `chatClient.ts`

Uses Axios (configured in `lib/axios.ts`) to call the chat service directly:

| Endpoint | Method | Description |
| --- | --- | --- |
| `/api/get-chats` | GET | List user's chats |
| `/api/create-chat` | POST | Create a new chat |
| `/api/allowed-users` | GET | Users available to chat with |
| `/api/chats/{id}/title` | PUT | Update chat title |
| `/api/chats/{id}/users` | POST/DELETE | Add/remove chat members |

### Notification Service — `notificationClient.ts`

Called directly for REST, plus WebSocket:

| Endpoint | Method | Description |
| --- | --- | --- |
| `/notifications` | GET | Paginated notifications |
| `/notifications/unread-count` | GET | Current unread count |
| `/notifications/{id}/read` | PATCH | Mark one as read |
| `/notifications/read-all` | POST | Mark all as read |
| `/notification-preferences` | GET/PUT | Notification type toggles |
| `/user-post-subscriptions/{authorId}` | GET/PUT | Post notification subscriptions |

## WebSocket Connections

Three separate WebSocket connections are maintained:

| Connection | URL | Protocol | Purpose |
| --- | --- | --- | --- |
| Chat messages | `WS_CHAT_SERVICE_URL/ws/{chatId}` | Binary (Protobuf) | Real-time messaging |
| Notifications | `NOTIFICATION_URL/ws?token=<jwt>` | JSON | New notification alerts, unread count |
| Chat unread | `NOTIFICATION_URL/ws/chat?token=<jwt>` | JSON | Per-chat unread count updates |

All WebSocket hooks implement auto-reconnect with exponential backoff (max 30 seconds).

## Request Authentication

All authenticated requests include an `Authorization: Bearer <token>` header. The token is read from cookies:

- **Client-side** — `Cookies.get("token")` via js-cookie.
- **Server-side** — `cookies().get("token")` via next/headers.

### 401 Handling

The Axios instance in `lib/axios.ts` has a response interceptor that redirects to `/authentication?expired=true` on any `401` response.

## GraphQL Queries

Query functions in `/queries`:

| File | Query | Returns |
| --- | --- | --- |
| `getPosts.ts` | Paginated feed posts | `Post[]` with cursor |
| `getProfile.ts` | User profile, followers, following, block status | `User`, `User[]`, `boolean` |
| `getPostById.ts` | Single post with full details | `CompletePost` |
| `getReportedFiles.ts` | Flagged media for admin | `ReportedFile[]` |
