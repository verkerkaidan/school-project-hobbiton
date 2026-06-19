defmodule MediaServiceElxWeb.Plugs.JWTPlug do
  @moduledoc """
  Validates a Bearer JWT from the Authorization header.

  Expects HMAC-256 signed tokens with:
    - "userId" (UUID string)
    - "role"   (list of strings, e.g. ["ROLE_ADMIN"])

  On success, assigns `:current_user` to the conn:
    %{user_id: uuid_string, roles: ["ROLE_ADMIN", ...]}

  On failure, halts with 401.
  """

  import Plug.Conn
  import Phoenix.Controller, only: [json: 2]

  @behaviour Plug

  def init(opts), do: opts

  def call(conn, _opts) do
    with {:ok, token}  <- extract_token(conn),
         {:ok, claims} <- verify_token(token) do
      assign(conn, :current_user, claims)
    else
      {:error, reason} ->
        conn
        |> put_status(:unauthorized)
        |> json(%{error: reason})
        |> halt()
    end
  end

  # ---------------------------------------------------------------------------
  # Private helpers
  # ---------------------------------------------------------------------------

  defp extract_token(conn) do
    case get_req_header(conn, "authorization") do
      ["Bearer " <> token | _] -> {:ok, String.trim(token)}
      _                        -> {:error, "missing or malformed Authorization header"}
    end
  end

  defp verify_token(token) do
    secret  = Application.fetch_env!(:media_service_elx, :jwt_secret)
    issuer  = Application.fetch_env!(:media_service_elx, :jwt_issuer)

    case JOSE.JWT.verify_strict(JOSE.JWK.from_oct(secret), ["HS256"], token) do
      {true, %JOSE.JWT{fields: claims}, _jws} ->
        with :ok <- validate_issuer(claims, issuer) do
          parse_claims(claims)
        end

      _ ->
        {:error, "invalid or expired token"}
    end
  end

  defp validate_issuer(%{"iss" => iss}, expected) when iss == expected, do: :ok
  defp validate_issuer(%{"iss" => _}, _), do: {:error, "token issuer mismatch"}
  defp validate_issuer(_, _), do: {:error, "token issuer claim missing"}

  defp parse_claims(%{"userId" => user_id} = claims) when is_binary(user_id) and user_id != "" do
    roles =
      case Map.get(claims, "role") do
        list when is_list(list) -> Enum.filter(list, &is_binary/1)
        _                       -> []
      end

    {:ok, %{user_id: user_id, roles: roles}}
  end

  defp parse_claims(_), do: {:error, "userId claim missing or invalid"}
end