defmodule MediaServiceElx.Repo.Migrations.DropUnusedColumnsRegardingTimestamps do
  use Ecto.Migration

  def up do
    execute "ALTER TABLE file_report DROP COLUMN IF EXISTS inserted_at"
    execute "ALTER TABLE file_report DROP COLUMN IF EXISTS updated_at"
    execute "ALTER TABLE media_files DROP COLUMN IF EXISTS inserted_at"
    execute "ALTER TABLE media_files DROP COLUMN IF EXISTS updated_at"
    execute "ALTER TABLE media_instances DROP COLUMN IF EXISTS inserted_at"
    execute "ALTER TABLE media_instances DROP COLUMN IF EXISTS updated_at"
    execute "ALTER TABLE reported_file DROP COLUMN IF EXISTS inserted_at"
    execute "ALTER TABLE reported_file DROP COLUMN IF EXISTS updated_at"
  end

  def down, do: :ok
end
