---
sidebar_position: 6
---

# Unit Tests

The Notification Service has Go unit tests for the core HTTP and WebSocket hub logic.

## What is tested

- Creating notifications validates required fields, respects disabled user preferences, persists enabled notifications, and returns the persisted payload shape.
- Batch notification creation only creates notifications for recipients with enabled `USER_POST` preferences and an active author subscription, while reporting skipped recipients.
- Listing notifications applies pagination defaults and returns rows with total counts.
- Read-state endpoints update a single notification, mark all unread notifications as read, and return unread counts.
- Notification preferences return stored values plus default-enabled values for missing types, upsert valid types, and reject invalid types.
- User-post subscription endpoints report current subscription state and toggle subscriptions on or off.
- Authentication-protected endpoints reject missing JWTs, and CORS preflight responses allow configured origins.
- Preference lookup defaults to enabled when no preference exists and fails open on database errors.
- The WebSocket hub delivers messages to registered users, unregisters clients, and replaces an existing connection for the same user.

The tests use `sqlmock` instead of PostgreSQL so they stay fast and deterministic in CI.

## Not tested

- PostgreSQL itself, migrations against a live database, and third-party WebSocket library behavior.
- Exhaustive JSON decoder behavior beyond malformed or incomplete request paths that influence service decisions.
- Browser-level notification rendering, which belongs to frontend or end-to-end tests.

## Latest result

Command run from `apps/notification-service`:

```bash
GOCACHE=/Users/dionaidan/Development/01-bpset-p4/.gocache go test ./...
```

Result on June 15, 2026:

```text
ok   notification-service/internal/handlers  0.995s
ok   notification-service/internal/hub       0.425s
```

Packages without test files also compiled successfully.
