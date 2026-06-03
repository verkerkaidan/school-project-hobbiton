defmodule MediaServiceElx.Repo.Migrations.CreateReportedFile do
  use Ecto.Migration

  def change do
    create_if_not_exists table(:reported_file, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :hash, :binary

      timestamps(type: :utc_datetime)
    end
  end
end
