defmodule MediaServiceElx.MediaFiles.MediaFile do
  @moduledoc """
Model for an uploaded media file
"""

  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  @content_flags %{
    "SAFE" => 0,
    "RISK" => 1,
    "UNASSIGNED" => 2
  }

  def content_flag(key), do: @content_flags[key]

  schema "media_files" do
    field :content_flag, :integer
    field :content_type, :string
    field :hash, :binary
    field :original, :binary
    field :admin_approved, :boolean, default: false
  end

  @doc false
  def changeset(media_file, attrs) do
    media_file
    |> cast(attrs, [:content_flag, :content_type, :hash, :original, :admin_approved])
    |> validate_required([:content_flag, :content_type, :hash, :original])
    |> unique_constraint(:hash)
    |> validate_inclusion(:content_flag, Map.values(@content_flags))
  end
end
