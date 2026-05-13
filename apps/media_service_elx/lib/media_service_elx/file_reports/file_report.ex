defmodule MediaServiceElx.FileReports.FileReport do
  @moduledoc """
Model for when a user has reported a file
"""

  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  schema "file_report" do
    field :file_id, :binary
    field :user_id, :binary
  end

  @doc false
  def changeset(file_report, attrs) do
    file_report
    |> cast(attrs, [:file_id, :user_id])
    |> validate_required([:file_id, :user_id])
  end
end
