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
      case find_media_file_by_hash(hash) do
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

  defp compute_hash(path) do
    File.stream!(path, [], 2048)
    |> Enum.reduce(:crypto.hash_init(:sha256), &:crypto.hash_update(&2, &1))
    |> :crypto.hash_final()
  end

  defp banned?(hash) do
    Repo.exists?(ReportedFile, where: hash == hash)
  end

  defp find_media_file_by_hash(hash) do
    Repo.get_by(MediaFile, hash: hash)
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