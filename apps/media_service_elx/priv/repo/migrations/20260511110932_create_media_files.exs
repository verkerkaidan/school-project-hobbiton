defmodule MediaServiceElx.Repo.Migrations.CreateMediaFiles do
  use Ecto.Migration

  def change do
    create_if_not_exists table(:media_files, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :content_flag, :integer
      add :content_type, :string
      add :hash, :binary
      add :original, :binary
      add :thumbnail, :binary

      timestamps(type: :utc_datetime)
    end

    create_if_not_exists unique_index(:media_files, [:hash])
  end
end
