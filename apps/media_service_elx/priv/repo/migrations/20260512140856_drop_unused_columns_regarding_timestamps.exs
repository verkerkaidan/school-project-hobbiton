defmodule MediaServiceElx.Repo.Migrations.DropUnusedColumnsRegardingTimestamps do
  use Ecto.Migration

  def change do
    alter table(:file_report) do
      remove :inserted_at
      remove :updated_at
    end
    alter table(:media_files) do
      remove :inserted_at
      remove :updated_at
    end
    alter table(:media_instances) do
      remove :inserted_at
      remove :updated_at
    end
    alter table(:reported_file) do
      remove :inserted_at
      remove :updated_at
    end
  end
end
