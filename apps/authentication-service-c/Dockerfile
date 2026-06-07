FROM debian:bookworm-slim

RUN apt-get update && \
    apt-get install -y \
        gcc \
        make \
        libmicrohttpd-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

RUN make

EXPOSE 21527

CMD ["./microservice"]
