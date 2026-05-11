defmodule MediaServiceElx.MediaFiles.MediaFile do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  schema "media_files" do
    field :content_flag, :integer
    field :content_type, :string
    field :hash, :binary
    field :original, :binary
    field :thumbnail, :binary

    timestamps(type: :utc_datetime)
  end

  @doc false
  def changeset(media_file, attrs) do
    media_file
    |> cast(attrs, [:content_flag, :content_type, :hash, :original, :thumbnail])
    |> validate_required([:content_flag, :content_type, :hash, :original, :thumbnail])
    |> unique_constraint(:hash)
  end
end
