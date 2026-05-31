defmodule MediaServiceElx.Repo.Migrations.AddingAdminApprovedLabelToFiles do
  use Ecto.Migration

  def change do
    alter table(:media_files) do
      add :admin_approved, :boolean, default: false
    end
  end
end
