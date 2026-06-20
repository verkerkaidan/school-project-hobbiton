---
sidebar_position: 3
---

# HTTP API

## Service Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | none | Register a new user with email and password. |
| `POST` | `/api/auth/login` | none | Authenticate and receive a JWT. |
| `PUT` | `/api/auth/email` | bearer token | Update the authenticated user's email. |
| `PUT` | `/api/auth/password` | bearer token | Update the authenticated user's password. |
| `DELETE` | `/api/auth/delete/{userId}` | bearer token | Delete a user by ID (self or ADMIN). |
| `PUT` | `/api/auth/admin/new/{userId}` | admin bearer token | Promote a user to `ROLE_ADMIN`. |
| `PUT` | `/api/auth/staff/new/{userId}` | admin bearer token | Promote a user to `ROLE_STAFF`. |
| `GET` | `/api/Oauth/dashboard` | bearer token | Protected dashboard returning welcome message. |
| `GET` | `/api/Oauth/me` | bearer token | Return current user ID and roles from the JWT. |
| `GET` | `/api/auth/metrics` | none | Prometheus-style metrics (request counts, active connections). |

All service paths use the `/api/` prefix. The gateway forwards requests directly to the same paths.

## Payloads

Register/Login request:

```json
{
  "email": "user@example.com",
  "password": "Str0ng!Pass"
}
```

Register/Login response:

```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Email update request:

```json
{
  "email": "newemail@example.com"
}
```

Password update request:

```json
{
  "currentPassword": "OldPass1!",
  "newPassword": "NewPass2!"
}
```

Delete request:

```json
{
  "email": "user@example.com",
  "password": "Str0ng!Pass"
}
```

Delete response:

```json
{
  "result": "Successfully removed user"
}
```

Admin/staff promotion response:

```json
{
  "result": "Successfully made user admin"
}
```

Dashboard response:

```json
{
  "message": "Welcome, a1b2c3d4-...",
  "roles": "ROLE_ADMIN"
}
```

Me response:

```json
{
  "id": "a1b2c3d4-...",
  "roles": "ROLE_USER"
}
```

## Notes

- Register requires a valid email (contains `@` and a dot after it) and a password of at least 8 characters with at least one digit, one lowercase, one uppercase, and one special character.
- The JWT expires 6 hours after issuance.
- Admin and staff promotion endpoints verify that the caller's JWT contains `ROLE_ADMIN`.
- The delete endpoint validates the JWT, then re-authenticates the user with email and password before deleting.
- The `/api/auth/metrics` endpoint exposes Prometheus-formatted counters for total requests, active connections, and per-route request counts.
