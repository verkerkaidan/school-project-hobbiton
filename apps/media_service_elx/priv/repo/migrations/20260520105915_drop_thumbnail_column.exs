defmodule MediaServiceElx.Repo.Migrations.DropThumbnailColumn do
  use Ecto.Migration

  def up do
    execute "ALTER TABLE media_files DROP COLUMN IF EXISTS thumbnail"
  end

  def down, do: :ok
end
