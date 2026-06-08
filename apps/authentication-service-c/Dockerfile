# Multi-stage Dockerfile for authentication-service-c
# Build stage: install build deps and compile the binary using the repo Makefile
FROM debian:bookworm-slim AS build

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        libpq-dev \
        ca-certificates \
        netcat-openbsd \
        pkg-config \
        curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Copy project files into image
COPY . .

# Build using the Makefile (produces ./auth-service)
RUN make

# Runtime stage: minimal runtime image with libpq available
FROM debian:bookworm-slim AS runtime

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libpq5 \
        ca-certificates \
        netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Copy binary from build stage
COPY --from=build /app/auth-service-c /app/auth-service-c

# Expose the port used by the service
EXPOSE 21525

# Wait for DB host/port and run the service. Use env DB_HOST/DB_PORT from compose if provided.
ENV DB_HOST=database
ENV DB_PORT=5432

CMD ["sh", "-c", "until nc -z ${DB_HOST} ${DB_PORT}; do echo 'waiting for db...'; sleep 1; done; exec ./auth-service-c"]
