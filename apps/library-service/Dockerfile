FROM rust:1.96-slim AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y pkg-config libssl-dev && rm -rf /var/lib/apt/lists/*

COPY Cargo.toml Cargo.lock ./
COPY src ./src
COPY migrations ./migrations

RUN cargo build --release

FROM rust:1.96-slim
WORKDIR /app

COPY --from=builder /app/target/release/library_service .
COPY --from=builder /app/migrations ./migrations

CMD ["./library_service"]