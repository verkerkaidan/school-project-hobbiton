defmodule MediaServiceElx.MediaServiceBehaviour do
  @callback upload_media_file(upload :: any(), owner_id :: String.t()) ::
              {:ok, String.t()} | {:error, atom()}

  @callback retrieve_unassigned() ::
              [MediaServiceElx.MediaFiles.MediaFile.t()]

  @callback retrieve_instances_by_owner(owner_id :: String.t()) ::
              [MediaServiceElx.MediaFiles.MediaFile.t()]

  @callback retrieve_media_file(instance_id :: String.t()) ::
              {:ok, MediaServiceElx.MediaFiles.MediaFile.t()} | {:error, atom()}

  @callback delete_file(media_file :: MediaServiceElx.MediaFiles.MediaFile) ::
              {:ok, String.t()} | {:error, atom()}

  @callback delete_single_instance(instance_id :: String.t()) ::
              {:ok, String.t()} | {:error, atom()}

  @callback delete_all_instances(instances :: any()) ::
              [{String.t(), String.t()}]

  @callback banned?(hash :: binary) ::
              true | false

  @callback retrieve_media_files_from_file_id(file_ids :: any()) ::
              {[MediaServiceElx.MediaFiles.MediaFile]}

  @callback update_content_flag(flag :: atom(), hash :: String.t(), note :: String.t()) ::
              {:ok, String.t()} | {:error, atom()}

end