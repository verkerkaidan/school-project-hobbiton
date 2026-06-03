defmodule MediaServiceElx.Repo.Migrations.DropUnusedColumnsRegardingTimestamps do
  use Ecto.Migration

  def change do
    alter table(:file_report) do
      remove_if_exists :inserted_at
      remove_if_exists :updated_at
    end
    alter table(:media_files) do
      remove_if_exists :inserted_at
      remove_if_exists :updated_at
    end
    alter table(:media_instances) do
      remove_if_exists :inserted_at
      remove_if_exists :updated_at
    end
    alter table(:reported_file) do
      remove_if_exists :inserted_at
      remove_if_exists :updated_at
    end
  end
end
