defmodule MediaServiceElx.Repo.Migrations.CreateMediaInstances do
  use Ecto.Migration

  def change do
    create_if_not_exists table(:media_instances, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :file_id, :binary
      add :owner_id, :binary

      timestamps(type: :utc_datetime)
    end
  end
end
