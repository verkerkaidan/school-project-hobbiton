FROM elixir:1.19.5-otp-27-alpine AS build
WORKDIR /app

# Install build dependencies
RUN apk add --no-cache build-base git

# Install hex and rebar
RUN mix local.hex --force && \
    mix local.rebar --force

ENV MIX_ENV=prod

# Copy dependency files and fetch deps
COPY mix.exs mix.lock ./
RUN mix deps.get --only prod

# Copy config before compiling deps
COPY config config
RUN mix deps.compile

# Copy source and build release
COPY lib lib
COPY priv priv

RUN mix compile
RUN mix release

# Runtime stage
FROM elixir:1.19.5-otp-27-alpine as runner
WORKDIR /app

RUN apk add --no-cache libstdc++ openssl ncurses-libs

COPY --from=build --chown=nobody:root /app/_build/prod/rel/media_service_elx ./
COPY --chown=nobody:root ./rel/overlays/bin/migrate_and_server ./bin/
RUN sed -i 's/\r//' ./bin/migrate_and_server && chmod +x ./bin/migrate_and_server

USER nobody

ENV PHX_SERVER=true

ENTRYPOINT ["./bin/migrate_and_server"]