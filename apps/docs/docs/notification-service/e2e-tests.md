---
sidebar_position: 7
---

# End-to-End Tests

The Notification Service is covered by Playwright tests in `tests/notifications.spec.ts`. The tests run against the full Docker Compose stack and exercise the frontend, API Gateway, and Notification Service together.

## Prerequisites

- All services running via `docker-compose up -d`.
- Frontend available at `http://localhost:19293`.
- Playwright installed (`npm install` in the project root).

## What is tested

### Empty state

- A freshly registered user sees the "No notifications yet" message on the `/notifications` page.
- The "Mark all as read" button is not visible when there are no notifications.

### Notification preferences

- The Settings page shows toggles for Follows, Posts from followed users, Likes, and Comments.
- Clicking a toggle changes its visual state, confirming the preference upsert round-trips through the API.

### Follow notification

- User B follows User A.
- User A navigates to `/notifications` and sees a follow notification mentioning User B's username.
- Clicking "Mark all as read" clears the button, confirming the read-all endpoint works end to end.

### Like, comment, and followed-user-post notifications

- User A creates a post. User B likes and comments on it.
- User A follows User B and subscribes to post notifications via the bell icon.
- User B creates a new post.
- User A sees three notifications: "liked your post", "commented on your post", and a published-post notification from User B.

## Running

```bash
# All notification tests
npx playwright test tests/notifications.spec.ts

# A single test by title
npx playwright test -g "should receive follow notification"

# Headed mode for debugging
npx playwright test tests/notifications.spec.ts --headed
```
