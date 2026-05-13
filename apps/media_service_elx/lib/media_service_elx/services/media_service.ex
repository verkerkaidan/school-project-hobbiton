defmodule MediaServiceElx.MediaService do
  alias MediaServiceElx.Repo
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaInstances.MediaInstance
  alias MediaServiceElx.FileReports.ReportedFile

  def upload_media_file(%Plug.Upload{} = upload, owner_id) do
    hash = compute_hash(upload.path)

    if banned?(hash) do
      {:error, :banned}
    else
      case Repo.get_by(MediaFile, hash: hash) do
        nil ->
          Repo.transaction(fn ->
            {:ok, media_file} = create_media_file(upload, hash)
            {:ok, instance} = create_media_instance(media_file.id, owner_id)
            instance.id
          end)
        media_file ->
          {:ok, instance} = create_media_instance(media_file.id, owner_id)
          {:ok, instance.id}
      end
    end
  end

  def delete_file(%MediaFile{} = media_file) do
    instances_list = Repo.all_by(MediaInstance, file_id: media_file.id)
    for(%MediaInstance{} = instance <- instances_list,
      do: delete_single_instance(instance)
    )
    case Repo.delete(media_file) do
      {:ok, _} -> {:ok, media_file.id}
      {:error, _} = err -> err
    end
  end

  def delete_single_instance(%MediaInstance{} = media_instance) do
    Repo.delete(media_instance)
  end

  def banned?(hash) do
    case Repo.get_by(ReportedFile, hash: hash) do
      nil -> false
      %ReportedFile{} -> true
    end
  end

  defp compute_hash(path) do
    File.stream!(path, [], 2048)
    |> Enum.reduce(:crypto.hash_init(:sha256), &:crypto.hash_update(&2, &1))
    |> :crypto.hash_final()
  end

  defp create_media_file(upload, hash) do
    binary = File.read!(upload.path)

    %MediaFile{}
    |> MediaFile.changeset(%{
      content_type: upload.content_type,
      hash: hash,
      original: binary,
      content_flag: MediaFile.content_flag(:unassigned)
    })
    |> Repo.insert()
  end

  defp create_media_instance(media_file_id, owner_id) do
    %MediaInstance{}
    |> MediaInstance.changeset(%{
      file_id: media_file_id,
      owner_id: owner_id
    })
    |> Repo.insert()
  end
end