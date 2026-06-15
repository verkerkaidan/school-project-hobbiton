---
sidebar_position: 3
---

# HTTP API

The recognition service exposes a REST endpoint for media tagging

## Endpoints

| Method | Path          | Body                                  | Returns                                                                  | Description                                                               |
|--------|---------------|---------------------------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------|
| `POST`  | `/recognition` | `{[{hash: string, file: string}]}` | `{[{hash: string, contentFlag: ContentFlag, note: "AI driven change"}]}` | Assigns a ContentFlag.SAFE or ContentFlag.RISK to each of the given files |

# Models

- ContentFlag:
    - `SAFE` 0
    - `RISK` 1
    - `UNASSIGNED` 2 (Not relevant in the recognition service)

## Notes

- The service does not authenticate requests itself; it assumes the caller forwards valid user identifiers.
