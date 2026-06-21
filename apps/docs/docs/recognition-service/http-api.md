---
sidebar_position: 3
---

# HTTP API

The recognition service exposes a REST endpoint for media tagging.

## Endpoints

| Method | Path               | Body                                    | Returns                                                                  | Description                                                               |
|--------|--------------------|-----------------------------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------|
| `POST` | `/api/recognition` | `[{hash: string, file: base64-string}]` | `[{hash: string, contentFlag: ContentFlag, note: "AI driven change"}]`   | Assigns `ContentFlag.SAFE` or `ContentFlag.RISK` to each supplied image   |

# Models

- ContentFlag:
    - `SAFE` 0
    - `RISK` 1
    - `UNASSIGNED` 2 (Not relevant in the recognition service)

## Notes

- The service permits all requests at its own Spring Security layer and relies on the API gateway/internal callers to control access.
- The `file` value must be a base64 encoded image. Invalid or unsupported image bytes cause inference to fail.
