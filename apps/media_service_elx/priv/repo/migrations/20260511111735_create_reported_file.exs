defmodule MediaServiceElx.Repo.Migrations.CreateReportedFile do
  use Ecto.Migration

  def change do
    create table(:reported_file, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :hash, :binary

      timestamps(type: :utc_datetime)
    end
  end
end
