defmodule MediaServiceElx.Repo.Migrations.CreateFileReport do
  use Ecto.Migration

  def change do
    create_if_not_exists table(:file_report, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :file_id, :binary
      add :user_id, :binary

      timestamps(type: :utc_datetime)
    end
  end
end
