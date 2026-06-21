---
sidebar_position: 6
---

# Unit Tests

The Bug Report Service has Go unit tests covering HTTP handlers, CORS, and JWT validation.

## What is tested

- Creating a bug report validates the JWT, trims whitespace from title and description, rejects empty fields, and returns the persisted record with status `OPEN`.
- Listing bug reports returns all reports ordered by `created_at DESC`, filters by `OPEN` or `CLOSED` status when the query parameter is present, and returns an empty array when no reports exist.
- Closing a bug report updates the row to `CLOSED` with a `closed_at` timestamp and returns the updated record, returns 404 when the report is missing or already closed, and returns 400 when the path ID is missing.
- CORS middleware sets `Access-Control-Allow-Origin` for allowed origins, omits the header for disallowed origins, and responds with 204 for OPTIONS preflight without invoking the inner handler.
- Authentication rejects missing tokens and invalid tokens with 401, and accepts valid HMAC-256 tokens.
- JWT validation rejects empty tokens, wrong secrets, expired tokens, malformed tokens, and non-HMAC signing algorithms.

The tests use `sqlmock` instead of PostgreSQL so they stay fast and deterministic in CI.

## Not tested

- PostgreSQL itself, migrations against a live database, and database bootstrapping logic.
- Prometheus metrics recording and the `/metrics` endpoint.
- End-to-end request flows through the API Gateway.
