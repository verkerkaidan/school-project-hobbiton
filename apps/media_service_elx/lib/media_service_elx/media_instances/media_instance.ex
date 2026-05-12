defmodule MediaServiceElx.MediaInstances.MediaInstance do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id
  schema "media_instances" do
    field :file_id, :binary
    field :owner_id, :binary
    belongs_to :mediaFile, MediaServiceElx.MediaFiles.MediaFile
  end

  @doc false
  def changeset(media_instance, attrs) do
    media_instance
    |> cast(attrs, [:file_id, :owner_id])
    |> validate_required([:file_id, :owner_id])
  end
end
