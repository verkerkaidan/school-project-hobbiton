defmodule MediaServiceElx.Repo do
  use Ecto.Repo,
    otp_app: :media_service_elx,
    adapter: Ecto.Adapters.Postgres
end
