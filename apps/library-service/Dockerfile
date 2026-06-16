FROM rust:1.96-slim AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*

COPY Cargo.toml Cargo.lock ./
COPY src ./src
COPY tests ./tests
COPY migrations ./migrations

RUN REDIS_URL=redis://127.0.0.1:1 cargo test
RUN cargo build --release

FROM rust:1.96-slim
WORKDIR /app

COPY --from=builder /app/target/release/library_service .
COPY --from=builder /app/migrations ./migrations

CMD ["./library_service"]