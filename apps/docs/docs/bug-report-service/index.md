---
sidebar_position: 1
---

# Bug Report Service

The **Bug Report Service** accepts authenticated user bug reports and gives administrators a small review workflow for listing and closing reports.

## Responsibilities

- Validate that report submitters have a valid bearer token
- Store anonymous bug reports in PostgreSQL
- List all reports for administrator views
- Filter reports by `OPEN` or `CLOSED`
- Close reports by setting status to `CLOSED` and recording `closed_at`
- Bootstrap the service database/user when PostgreSQL admin credentials are provided

## Ports

| Runtime | Port | Notes |
| --- | --- | --- |
| Local/default service port | `29556` | Used when `PORT` is not set. |
| Docker container port | `29556` | Exposed by `bug-report-service`. |
| Docker host port | `29556` | Mapped as `29556:29556`. |
| PostgreSQL port | `5432` | The service connects to the shared `database` container. |

The API Gateway reaches this service through `BUG_REPORT_SERVICE_URL`.

## Technology Stack

- **Language:** Go
- **Database:** PostgreSQL
- **Transport:** HTTP
