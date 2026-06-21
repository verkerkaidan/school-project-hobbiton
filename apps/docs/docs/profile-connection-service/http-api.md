---
sidebar_position: 3
---

# HTTP API

All routes are served under the `/api` context path.

## Follow Endpoints

| Method | Path | Headers | Description |
| --- | --- | --- | --- |
| `PUT` | `/api/follows/{targetProfileId}` | `X-Profile-Id: <currentProfileId>` | Toggle follow/unfollow. Returns `true` when now following, `false` when unfollowed. |
| `GET` | `/api/follows/{profileId}/followers` | none | Return profile IDs that follow `profileId`. |
| `GET` | `/api/follows/{profileId}/following` | none | Return profile IDs followed by `profileId`. |
| `DELETE` | `/api/follows/user/{userUUID}` | none | Delete all follow records where the user is follower or target and write an audit log. |

## Block Endpoints

| Method | Path | Headers | Description |
| --- | --- | --- | --- |
| `PUT` | `/api/blocks/{targetProfileId}` | `X-Profile-Id: <currentProfileId>` | Toggle block/unblock. Returns `true` when now blocking, `false` when unblocked. |
| `GET` | `/api/blocks/{profileId}/blocked` | none | Return profile IDs blocked by `profileId`. |
| `GET` | `/api/blocks/{profileId}/blocked-by` | none | Return profile IDs that have blocked `profileId`. |
| `DELETE` | `/api/blocks/user/{userUUID}` | none | Delete all block records where the user is blocker or target and write an audit log. |

## Report Endpoints

| Method | Path | Body | Description |
| --- | --- | --- | --- |
| `POST` | `/api/reports` | Create report payload | Create a profile report. |
| `GET` | `/api/reports/{reportId}` | none | Fetch one report by ID. |
| `GET` | `/api/reports/target/{targetId}` | none | Fetch reports about a target profile. |
| `GET` | `/api/reports/initiator/{initiatorId}` | none | Fetch reports created by a profile. |
| `GET` | `/api/reports/all` | none | Fetch all reports in the system. |
| `GET` | `/api/reports/status/{status}` | none | Fetch reports by status. |
| `GET` | `/api/reports/pending` | none | Fetch reports with `PENDING` status. |
| `PUT` | `/api/reports/{reportId}/status` | `{ "status": "RESOLVED" }` | Update report status. |
| `DELETE` | `/api/reports/{reportId}` | none | Delete one report. |
| `DELETE` | `/api/reports/user/{userUUID}` | none | Delete all report records where the user is initiator or target and write an audit log. |

## Create Report Payload

```json
{
  "initiatorId": "reporter-profile-uuid",
  "targetId": "reported-profile-uuid",
  "reason": "HARASSMENT",
  "description": "The reported profile is sending repeated unwanted messages."
}
```

Supported reasons:

- `HARASSMENT`
- `HATE_SPEECH`
- `SPAM`
- `INAPPROPRIATE_CONTENT`
- `MISINFORMATION`
- `IMPERSONATION`
- `SCAM_FRAUD`
- `VIOLENCE`
- `SEXUAL_CONTENT`
- `OTHER`

Supported statuses:

- `PENDING`
- `UNDER_REVIEW`
- `RESOLVED`
- `DISMISSED`
- `ACTION_TAKEN`

## Deletion Response

```json
{
  "deletedCount": 3,
  "auditLogId": "audit-log-uuid",
  "timestamp": "2026-06-15T10:00:00",
  "message": "Successfully deleted 3 follow records for user user-uuid"
}
```

## Notes

- The service currently permits all requests at the Spring Security layer and relies on caller-provided profile IDs.
- Mutating follow/block endpoints use the `X-Profile-Id` header to identify the acting profile.
- CORS allows origins from `CORS_ALLOWED_ORIGINS`.
