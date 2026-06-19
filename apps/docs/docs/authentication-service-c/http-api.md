---
sidebar_position: 3
---

# HTTP API

## Service Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | none | Register a new user with email and password. |
| `POST` | `/auth/login` | none | Authenticate and receive a JWT. |
| `DELETE` | `/auth/delete` | bearer token | Delete own account (requires email + password in body). |
| `PUT` | `/auth/admin/new/{userId}` | admin bearer token | Promote a user to `ROLE_ADMIN`. |
| `PUT` | `/auth/staff/new/{userId}` | admin bearer token | Promote a user to `ROLE_STAFF`. |
| `GET` | `/Oauth/dashboard` | bearer token | Protected dashboard returning welcome message. |
| `GET` | `/Oauth/me` | bearer token | Return current user ID and roles from the JWT. |
| `DELETE` | `/auth/{userId}` | service-internal | Delete a user by ID (used by the gateway for rollback). |
| `GET` | `/auth/{userId}` | service-internal | Retrieve authentication data by user ID. |

## Gateway Routes

| Gateway method | Gateway path | Service path |
| --- | --- | --- |
| `POST` | `/api/auth/register` | `POST /auth/register` |
| `POST` | `/api/auth/login` | `POST /auth/login` |
| `DELETE` | `/api/auth/delete` | `DELETE /auth/delete` |
| `PUT` | `/api/auth/admin/new/{userId}` | `PUT /auth/admin/new/{userId}` |
| `PUT` | `/api/auth/staff/new/{userId}` | `PUT /auth/staff/new/{userId}` |

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
