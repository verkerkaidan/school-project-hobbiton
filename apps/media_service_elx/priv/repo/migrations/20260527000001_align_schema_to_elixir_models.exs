defmodule MediaServiceElx.Repo.Migrations.AlignSchemaToElixirModels do
  use Ecto.Migration

  def up do
    # media_files: rename file_id → id (PK constraint follows the column rename)
    execute "ALTER TABLE media_files RENAME COLUMN file_id TO id"

    # media_instances: PK was dropped by drop_java_columns; add id UUID PK,
    # convert file_id and owner_id from uuid → bytea (raw 16-byte representation)
    execute "ALTER TABLE media_instances ADD COLUMN id uuid DEFAULT gen_random_uuid() PRIMARY KEY"

    execute """
    ALTER TABLE media_instances
      ALTER COLUMN file_id  TYPE bytea USING decode(replace(file_id::text,  '-', ''), 'hex'),
      ALTER COLUMN owner_id TYPE bytea USING decode(replace(owner_id::text, '-', ''), 'hex')
    """

    # file_report: PK was dropped; add id UUID PK, convert file_id uuid → bytea,
    # add user_id bytea (userid was already dropped by drop_java_columns)
    execute "ALTER TABLE file_report ADD COLUMN id uuid DEFAULT gen_random_uuid() PRIMARY KEY"

    execute """
    ALTER TABLE file_report
      ALTER COLUMN file_id TYPE bytea USING decode(replace(file_id::text, '-', ''), 'hex')
    """

    execute "ALTER TABLE file_report ADD COLUMN IF NOT EXISTS user_id bytea"

    # reported_file: PK was dropped (file_id dropped by drop_java_columns);
    # add id UUID PK
    execute "ALTER TABLE reported_file ADD COLUMN id uuid DEFAULT gen_random_uuid() PRIMARY KEY"
  end

  def down, do: :ok
end
