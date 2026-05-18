defmodule MediaServiceElx.FileReports.ReportedFile do
  @moduledoc """
Model for when a file was banned
"""

  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  schema "reported_file" do
    field :hash, :binary
  end

  @doc false
  def changeset(reported_file, attrs) do
    reported_file
    |> cast(attrs, [:hash])
    |> validate_required([:hash])
  end
end
