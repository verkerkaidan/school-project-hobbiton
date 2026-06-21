defmodule MediaServiceElx.Repo.Migrations.ConvertAllNecessaryBinaryToBinaryId do
  use Ecto.Migration

  def up do
    ## Media_instances owner
    alter table(:media_instances) do
      add :owner_id_new, :binary_id
    end

    execute("""
    UPDATE media_instances
    SET owner_id_new = convert_from(owner_id, 'UTF8')::uuid
    """)

    alter table(:media_instances) do
      remove :owner_id
    end

    rename table(:media_instances), :owner_id_new, to: :owner_id

    ######################################################################

    ## File_report
    alter table(:file_report) do
      add :file_id_new, :binary_id
      add :user_id_new, :binary_id
    end

    execute("""
    UPDATE file_report
    SET file_id_new = convert_from(file_id, 'UTF8')::uuid
    """)
    execute("""
    UPDATE file_report
    SET user_id_new = convert_from(user_id, 'UTF8')::uuid
    """)

    alter table(:file_report) do
      remove :file_id
      remove :user_id
    end

    rename table(:file_report), :file_id_new, to: :file_id
    rename table(:file_report), :user_id_new, to: :user_id
  end

  def down do
    alter table(:file_report) do
      add :file_id_old, :binary
      add :user_id_old, :binary
    end

    execute("""
    UPDATE file_report
    SET file_id_old = convert_to(file_id::text, 'UTF8')
    """)
    execute("""
    UPDATE file_report
    SET user_id_old = convert_to(user_id::text, 'UTF8')
    """)

    alter table(:file_report) do
      remove :file_id
      remove :user_id
    end

    rename table(:file_report), :file_id_old, to: :file_id
    rename table(:file_report), :user_id_old, to: :user_id
  end
end
