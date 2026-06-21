---
sidebar_position: 7
---

# End-to-End Tests

The Bug Report Service is covered by Playwright tests in `tests/bug-report.spec.ts`. The tests run against the full Docker Compose stack and exercise the frontend, API Gateway, and Bug Report Service together.

## Prerequisites

- All services running via `docker-compose up -d`.
- Frontend available at `http://localhost:19293`.
- Playwright installed (`npm install` in the project root).

## What is tested

### Modal and form validation

- The "Report a bug" button on the Settings page opens a modal with title and description fields.
- The submit button is disabled when both fields are empty, when only the title is filled, and when only the description is filled.
- The submit button becomes enabled once both fields have content.
- Clicking "Cancel" closes the modal.

### Successful submission

- Filling in both fields and clicking "Submit report" sends the report through the gateway to the service.
- A success confirmation ("Report submitted" / "Thanks for letting us know") is displayed.
- Clicking "Close" dismisses the confirmation.

## Running

```bash
# All bug report tests
npx playwright test tests/bug-report.spec.ts

# Headed mode for debugging
npx playwright test tests/bug-report.spec.ts --headed
```
