---
sidebar_position: 4
---

# State Management

The frontend uses a hybrid approach — no single global store, but a combination of React Query, Context, hooks, cookies, and WebSockets.

## Overview

| Layer | Technology | Responsibility |
| --- | --- | --- |
| Server state | TanStack React Query | Posts, profiles, followers, paginated data |
| Real-time state | WebSocket + Context | Notifications, chat messages, unread counts |
| User identity | `useSyncExternalStore` + cookies | Current user session |
| UI state | `useState` / component-local | Modals, forms, filters, selections |
| Persistence | js-cookie | Token, userId, email |
| Theme | localStorage | Dark/light mode preference |

## React Query

Configured in `providers/QueryProvider.tsx`:

| Setting | Value |
| --- | --- |
| `staleTime` | `Infinity` (data never auto-refetches) |
| `gcTime` | 30 minutes |

Key query patterns:
- **`useInfiniteQuery`** — Feed page uses cursor-based pagination for infinite scroll.
- **`useQuery`** — Single post, profile, follower lists.
- **`queryClient.invalidateQueries`** — Used after mutations (block user → refetch feed).

## Custom Hooks

### `useCurrentUser`

Central user identity hook. Uses React's `useSyncExternalStore` to share a single user object across all components without Context.

- Reads `token`, `userId`, and `email` from cookies on first call.
- Fetches the full profile via GraphQL if cookies are present.
- Caches the result in a module-level variable — subsequent calls return immediately.
- Returns `CurrentUser | null` with: `id`, `username`, `email`, `token`, `isAdmin`.

### `useWebSocket`

Manages the chat WebSocket connection for a specific chat room.

- Connects to `NEXT_PUBLIC_WS_CHAT_SERVICE_URL/ws/{chatId}`.
- Encodes/decodes messages using **Protocol Buffers** (binary format).
- Auto-reconnects with exponential backoff (max 30 seconds).
- Exposes: `send()`, `deleteUser()`, `addUser()`, connection status, message history.

### `useNotificationSocket`

WebSocket connection for real-time notifications.

- Connects to `NEXT_PUBLIC_NOTIFICATION_URL/ws?token=<jwt>`.
- Receives JSON payloads for new notifications and unread count changes.
- Auto-reconnects with exponential backoff.
- Callbacks: `onNotification`, `onUnreadCountChange`, `onOpen`.

### `useChatNotificationSocket`

Separate WebSocket for chat unread count updates.

- Provides per-chat unread counts.
- Methods: `markChatAsRead(chatId)`, `markAllAsRead()`.
- Offline command queue — buffers commands while disconnected.

### `useToggleBlock`

Toggles user block/unblock status and invalidates the feed query.

### `useTheme`

Dark/light mode toggle. Persists to `localStorage`, applies a CSS class to `<html>`.

## Context Providers

### NotificationProvider

Wraps the main layout. Manages:
- Unread notification count (fetched on mount, updated via WebSocket).
- Subscriber pattern — components can register callbacks for new notifications.

### ChatNotificationProvider

Wraps the main layout. Manages:
- Per-chat unread counts as a `Map<chatId, count>`.
- Computed `unreadTotal` for the nav badge.
- Methods for marking chats as read.

### ProfilePicProvider

Caches the current user's profile picture URL to avoid redundant fetches.

### ErrorToastProvider

Provides a `showError(message)` function to display error toasts from anywhere in the app.

## Type Definitions

Key types in `/types`:

| File | Types |
| --- | --- |
| `posts.ts` | `Post`, `Gym`, `Reading`, `Other`, `Exercise`, `MediaSet`, `CompletePost`, `LinkedHobbyItem` |
| `profile.ts` | `User` (username, bio, profilePicture, follower counts) |
| `enums.ts` | `HobbyType` (GYM, READING, OTHER), `ContentFlag` (SAFE, RISK, UNASSIGNED) |
| `interaction.ts` | `Interaction`, `Comment`, `CompleteComment` |
| `notifications.ts` | `NotificationType`, `Notification`, `NotificationsResponse`, `NotificationPreferences` |
| `LinkedItem.ts` | `LinkedItem` (BOOK, EXERCISE, MOVIE) |
| `reports.ts` | `ReportReason`, `ReportStatus`, `Report`, `ReportedFile` |
| `interfaces.tsx` | `LoginForm`, `RegisterForm`, `BaseCreatePost`, `CreateGymPost`, `CreateReadingPost` |
