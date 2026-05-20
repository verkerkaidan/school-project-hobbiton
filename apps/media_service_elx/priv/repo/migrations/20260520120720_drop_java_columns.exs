defmodule MediaServiceElx.Repo.Migrations.DropJavaColumns do
  use Ecto.Migration

  def change do
    execute "ALTER TABLE file_report DROP COLUMN IF EXISTS report_id"
    execute "ALTER TABLE file_report DROP COLUMN IF EXISTS userid"
    execute "ALTER TABLE media_instances DROP COLUMN IF EXISTS instance_id"
    execute "ALTER TABLE reported_file DROP COLUMN IF EXISTS file_id"
  end
end
