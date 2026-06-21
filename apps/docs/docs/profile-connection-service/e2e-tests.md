---
sidebar_position: 7
---

# End-to-End Tests

The Profile Connection Service is covered by Playwright tests in `tests/profile-connections.spec.ts`. The tests run against the full Docker Compose stack and exercise the frontend, API Gateway, and Profile Connection Service together.

## Prerequisites

- All services running via `docker-compose up -d`.
- Frontend available at `http://localhost:19293`.
- Playwright installed (`npm install` in the project root).

## What is tested

### Own profile

- A user's own profile page does not show a Follow button.
- Follower and Following counts are visible and their modals open and close correctly.

### Follow and unfollow

- Clicking "Follow" on another user's profile changes the button to "Unfollow" and increments the follower count.
- Clicking "Unfollow" reverts the button to "Follow".

### Block

- Blocking a user via the profile settings menu changes the button to "User blocked".
- The blocker sees "profile is hidden because you have blocked" and "posts are hidden because you have blocked" messages on the blocked user's profile.
- The blocked user sees the same hidden-profile messages when viewing the blocker's profile.
- The blocked user appears in the "Blocked Users" list on the blocker's Settings page.

### Report

- Clicking "Report" on another user's profile opens a report modal pre-filled with the target username.
- Entering a description and clicking "Submit report" submits the report and closes the modal.

## Running

```bash
# All profile connection tests
npx playwright test tests/profile-connections.spec.ts

# A single test by title
npx playwright test -g "should block another user"

# Headed mode for debugging
npx playwright test tests/profile-connections.spec.ts --headed
```
