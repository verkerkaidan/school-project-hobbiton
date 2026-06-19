defmodule MediaServiceElxWeb.ConnCase do
  @moduledoc """
  This module defines the test case to be used by
  tests that require setting up a connection.

  Such tests rely on `Phoenix.ConnTest` and also
  import other functionality to make it easier
  to build common data structures and query the data layer.

  Finally, if the test case interacts with the database,
  we enable the SQL sandbox, so changes done to the database
  are reverted at the end of every test. If you are using
  PostgreSQL, you can even run database tests asynchronously
  by setting `use MediaServiceElxWeb.ConnCase, async: true`, although
  this option is not recommended for other databases.
  """

  use ExUnit.CaseTemplate

  import Plug.Conn

  using do
    quote do
      # The default endpoint for testing
      @endpoint MediaServiceElxWeb.Endpoint

      use MediaServiceElxWeb, :verified_routes

      # Import conveniences for testing with connections
      import Plug.Conn
      import Phoenix.ConnTest
      import MediaServiceElxWeb.ConnCase
    end
  end

  setup tags do
    MediaServiceElx.DataCase.setup_sandbox(tags)
    {:ok, conn: Phoenix.ConnTest.build_conn()}
  end

  def valid_jwt(user_id \\ Ecto.UUID.generate(), roles \\ ["ROLE_USER"]) do
    claims = %{
      "userId" => user_id,
      "role"   => roles,
      "iss"    => "test-issuer",
      "exp"    => System.system_time(:second) + 3600
    }

    jwk = JOSE.JWK.from_oct("test-secret")
    {_, token} = JOSE.JWT.sign(jwk, %{"alg" => "HS256"}, claims) |> JOSE.JWS.compact()
    token
  end

  def authed_conn(conn, opts \\ []) do
    user_id = Keyword.get(opts, :user_id, Ecto.UUID.generate())
    roles   = Keyword.get(opts, :roles, ["ROLE_USER"])
    put_req_header(conn, "authorization", "Bearer #{valid_jwt(user_id, roles)}")
  end
end
