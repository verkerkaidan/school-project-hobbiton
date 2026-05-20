defmodule MediaServiceElx.Repo.Migrations.DropThumbnailColumn do
  use Ecto.Migration

  def change do
    alter table(:media_files) do
      remove :thumbnail
    end
  end
end
