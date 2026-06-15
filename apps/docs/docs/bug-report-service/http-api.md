---
sidebar_position: 3
---

# HTTP API

## Service Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/health` | none | Liveness check. |
| `POST` | `/bug-reports` | bearer token | Create an anonymous bug report. |
| `GET` | `/bug-reports?status=OPEN` | gateway/admin caller | List all bug reports, optionally filtered by `OPEN` or `CLOSED`. |
| `PUT` | `/bug-reports/{id}/close` | gateway/admin caller | Close an open report and return the updated record. |

## Gateway Routes

| Gateway method | Gateway path | Service path |
| --- | --- | --- |
| `POST` | `/api/bug-reports` | `POST /bug-reports` |
| `GET` | `/api/admin/bug-reports` | `GET /bug-reports` |
| `PUT` | `/api/admin/bug-reports/{id}/close` | `PUT /bug-reports/{id}/close` |

## Payloads

Create request:

```json
{
  "title": "Notifications do not load",
  "description": "The notification panel stays empty after refresh."
}
```

Response:

```json
{
  "id": "7a094eda-9052-4434-acce-caa8d111f827",
  "title": "Notifications do not load",
  "description": "The notification panel stays empty after refresh.",
  "status": "OPEN",
  "createdAt": "2026-06-15T10:00:00Z",
  "closedAt": null
}
```

## Notes

- Reports are anonymous inside the service: the JWT proves the user is logged in, but no user ID is stored on the report.
- `GET /bug-reports` accepts only `OPEN` or `CLOSED` as meaningful status filters; other values return all reports.
- Closing a report only updates rows currently in `OPEN` status.
