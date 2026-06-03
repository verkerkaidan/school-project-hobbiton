import Config

if config_env() == :prod do
  maybe_ipv6 = if System.get_env("ECTO_IPV6") in ~w(true 1), do: [:inet6], else: []

  config :media_service_elx, MediaServiceElx.Repo,
         hostname: System.get_env("DB_HOST") || "localhost",
         port: System.get_env("DB_PORT") || "5432" |> String.to_integer(),
         database: System.get_env("DB_NAME") || "media_database",
         username: System.get_env("DB_USERNAME") || "postgres",
         password: System.get_env("DB_PASSWORD") || "postgres",
         pool_size: 10,
         socket_options: maybe_ipv6

  _secret_key_base = System.fetch_env!("SECRET_KEY_BASE")
  host = System.get_env("PHX_HOST") || "example.com"

  config :media_service_elx, :dns_cluster_query, System.get_env("DNS_CLUSTER_QUERY")

  config :media_service_elx, MediaServiceElxWeb.Endpoint,
         url: [host: host, port: 443, scheme: "https"],
         http: [ip: {0, 0, 0, 0, 0, 0, 0, 0}, port: String.to_integer(System.get_env("PORT") || "27089")],
         secret_key_base: System.fetch_env!("SECRET_KEY_BASE")

  config :media_service_elx, :media_service, MediaServiceElx.MediaService
  config :media_service_elx, :report_service, MediaServiceElx.ReportService
end

if System.get_env("PHX_SERVER") do
  config :media_service_elx, MediaServiceElxWeb.Endpoint, server: true
end
