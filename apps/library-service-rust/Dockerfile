# ─── Build stage ──────────────────────────────────────────────────────────────
FROM rust:1.80-slim as builder

WORKDIR /app
COPY Cargo.toml Cargo.lock ./
# Cache dependencies by building an empty main first (debug build for faster iteration)
RUN mkdir src && echo "fn main() {}" > src/main.rs && cargo build && rm -rf src

COPY src ./src
RUN touch src/main.rs && cargo build

# ─── Runtime stage ────────────────────────────────────────────────────────────
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/debug/library_service .

EXPOSE 28877

ENV RUST_LOG="library_service=info,tower_http=info"

CMD ["./library_service"]