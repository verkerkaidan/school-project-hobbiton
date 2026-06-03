# ─── Build stage ──────────────────────────────────────────────────────────────
FROM rust:1.96-slim as builder

WORKDIR /app

# Install musl tools for static linking
RUN apt-get update && apt-get install -y musl-tools && rm -rf /var/lib/apt/lists/*
RUN rustup target add x86_64-unknown-linux-musl

COPY Cargo.toml Cargo.lock ./
# Cache dependencies by building an empty main first
RUN mkdir src && echo "fn main() {}" > src/main.rs && cargo build --release --target x86_64-unknown-linux-musl && rm -rf src

COPY src ./src
RUN touch src/main.rs && cargo build --release --target x86_64-unknown-linux-musl

# ─── Runtime stage ────────────────────────────────────────────────────────────
FROM alpine:latest

RUN apk add --no-cache ca-certificates

WORKDIR /app
COPY --from=builder /app/target/x86_64-unknown-linux-musl/release/library_service .

EXPOSE 28877

ENV RUST_LOG="library_service=info,tower_http=info"

CMD ["./library_service"]