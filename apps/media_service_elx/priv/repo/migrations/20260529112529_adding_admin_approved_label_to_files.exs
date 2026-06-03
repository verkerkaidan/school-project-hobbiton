defmodule MediaServiceElx.Repo.Migrations.AddingAdminApprovedLabelToFiles do
  use Ecto.Migration

  def change do
    alter table(:media_files) do
      add_if_not_exists :admin_approved, :boolean, default: false
    end
  end
end
