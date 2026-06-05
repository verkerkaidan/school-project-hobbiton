FROM rust:1.96-slim as builder
WORKDIR /app

COPY Cargo.toml Cargo.lock ./
COPY src ./src

# dummy build voor cache (MAG, maar pas NA src copy)
RUN cargo build --release

FROM rust:1.96-slim
WORKDIR /app
COPY --from=builder /app/target/release/library_service .

CMD ["./library_service"]