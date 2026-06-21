defmodule MediaServiceElx.Repo.Migrations.ConvertBinaryToUuid do
  use Ecto.Migration

  def up do
    alter table(:media_instances) do
      add :file_id_new, :binary_id
    end

    execute("""
    UPDATE media_instances
    SET file_id_new = convert_from(file_id, 'UTF8')::uuid
    """)

    alter table(:media_instances) do
      remove :file_id
    end

    rename table(:media_instances), :file_id_new, to: :file_id
  end

  def down do
    alter table(:media_instances) do
      add :file_id_old, :binary
    end

    execute("""
    UPDATE media_instances
    SET file_id_old = convert_to(file_id::text, 'UTF8')
    """)

    alter table(:media_instances) do
      remove :file_id
    end

    rename table(:media_instances), :file_id_old, to: :file_id
  end
end
