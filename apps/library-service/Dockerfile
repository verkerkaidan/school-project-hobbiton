FROM rust:1.96-slim as builder
WORKDIR /app

COPY Cargo.toml Cargo.lock ./
COPY src ./src
COPY tests ./tests

# Run tests before building the release binary.
# If any test fails the entire Docker build fails,
# which blocks the pipeline from producing an image.
# REDIS_URL is pointed at an intentionally unreachable port so
# any test that needs Redis skips itself via the require_redis! macro,
# and unit/mock tests run without any external dependency.
RUN REDIS_URL=redis://127.0.0.1:1 cargo test

RUN cargo build --release

FROM rust:1.96-slim
WORKDIR /app
COPY --from=builder /app/target/release/library_service .

CMD ["./library_service"]