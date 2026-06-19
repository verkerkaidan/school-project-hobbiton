import Config

# Configure your database
#
# The MIX_TEST_PARTITION environment variable can be used
# to provide built-in test partitioning in CI environment.
# Run `mix help test` for more information.
config :media_service_elx, MediaServiceElx.Repo,
       username: System.get_env("POSTGRES_TEST_USER", "postgres"),
       password: System.get_env("POSTGRES_TEST_PASSWORD", "postgres"),
       hostname: System.get_env("POSTGRES_TEST_HOST", "localhost"),
       database: System.get_env("POSTGRES_TEST_DB", "media_service_test"),
       pool: Ecto.Adapters.SQL.Sandbox,
       pool_size: System.schedulers_online() * 2

# We don't run a server during test. If one is required,
# you can enable the server option below.
config :media_service_elx, MediaServiceElxWeb.Endpoint,
  http: [ip: {127, 0, 0, 1}, port: 4002],
  secret_key_base: "oC1SfQSZOUeBD4/4eXz/0+QHqbX7gpF7ABHzozZ82QAexrsX8kPiN8HLLDYBwwMf",
  server: false

# In test we don't send emails
config :media_service_elx, MediaServiceElx.Mailer, adapter: Swoosh.Adapters.Test

# Disable swoosh api client as it is only required for production adapters
config :swoosh, :api_client, false

# Print only warnings and errors during test
config :logger, level: :warning

# Initialize plugs at runtime for faster test compilation
config :phoenix, :plug_init_mode, :runtime

# Sort query params output of verified routes for robust url comparisons
config :phoenix,
  sort_verified_routes_query_params: true

config :media_service_elx, :media_service, MediaServiceElx.MediaServiceMock
config :media_service_elx, :report_service, MediaServiceElx.ReportServiceMock

config :media_service_elx,
       jwt_secret: "test-secret",
       jwt_issuer: "test-issuer"
