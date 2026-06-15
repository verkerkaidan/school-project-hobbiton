---
sidebar_position: 1
---

# Media Service (Elixir)

The **Media Service (Elixir)** is an alternative implementation of the media service.
The previous implementation was in Java Springboot, the current version is built with the Phoenix framework

## Overview

The service serves as the media backend for Hobbiton. It provides:

- Persistent media storage and retrieval
- Report creation for media content
- Deletion of media files and reports that are no longer relevant

## Port

| Container port | Host port |
|----------------|-----------|
| `27086`        | `27086`   |

## Responsibilities

- Accept multipart file uploads
- Store and serve media assets
- Store and serve media reports
- Provide user management for media ownership and report attribution

## Configuration

The service requires the following environment variables for configuration:
- `DB_HOST`: The hostname of the PostgreSQL database
- `DB_PORT`: The port number of the PostgreSQL database
- `DB_NAME`: The name of the PostgreSQL database
- `DB_USERNAME`: The username for connecting to the PostgreSQL database
- `DB_PASSWORD`: The password for connecting to the PostgreSQL database
- `SECRET_KEY_BASE`: A secret key for encrypting session data and other secrets in the Phoenix application

<nl>
These variables should be injected via environment variables at runtime
</nl>

## Technology Stack

- **Language:** Elixir
- **Framework:** Phoenix
- **Build tool:** Mix
- **Database:** PostgreSQL
