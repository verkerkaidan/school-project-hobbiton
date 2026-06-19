defmodule MediaServiceElx.MediaService do
  @moduledoc """
  Handles most logic regarding actions with media files and media instances
  """
  @behaviour MediaServiceElx.MediaServiceBehaviour

  require Logger

  alias MediaServiceElx.Repo
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaInstances.MediaInstance
  alias MediaServiceElx.FileReports.ReportedFile
  alias MediaServiceElx.ReportService

  @doc """
  Handles the upload of media files and thus the creation of MediaInstance and MediaFile when necessary
  @params: form-data file, uuid
  @returns: {:error, :banned} or {:ok, uuid}
  """
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

  def retrieve_unassigned() do
    Repo.all_by(MediaFile, content_flag: 2)
  end

  @doc """
    Retrieves all mediaInstances connected to the owner_id
    @params: owner_id uuid
    @returns: list of MediaInstances
  """
  def retrieve_instances_by_owner(owner_id) do
    Repo.all_by(MediaInstance, owner_id: owner_id)
  end

  @doc """
    Retrieves a mediaFile from a mediaInstance
    @params: media_instance uuid
    @returns: {:ok, %MediaFile{}}
  """
  def retrieve_media_file(instance_id) do
    case Repo.get_by(MediaInstance, id: instance_id) do
      %MediaInstance{} = media_instance ->
        case Repo.get_by(MediaFile, id: media_instance.file_id) do
          %MediaFile{} = media_file ->
            {:ok, media_file}
          nil ->
            Logger.error("MediaFile not found for instance", instance_id: instance_id, file_id: media_instance.file_id)
            {:error, "MediaFile not found"}
        end
      nil ->
        Logger.error("MediaInstance not found", instance_id: instance_id)
        {:error, "MediaInstance not found"}
    end
  end

  @doc """
    Handles the deletion of media files after a ban
    @params: media_file object
    @returns: {:ok, uuid}
  """
  def delete_file(%MediaFile{} = media_file) do
    instances_list = Repo.all_by(MediaInstance, file_id: media_file.id)
    for(%MediaInstance{} = instance <- instances_list,
      do: Repo.delete(instance)
    )
    case Repo.delete(media_file) do
      {:ok, _} ->
        Logger.info("MediaFile deleted", file_id: media_file.id)
        {:ok, media_file.id}
      {:error, reason} ->
        Logger.error("MediaFile could not be deleted", file_id: media_file.id, reason: reason)
        {:error, reason}
    end
  end

  @doc """
    Handles the deletion of media files and thus a MediaInstance and MediaFile when necessary
    @params: uuid
    @returns: {:ok, uuid}
  """
  def delete_single_instance(instance_id) do
    case Repo.get_by(MediaInstance, id: instance_id) do
      %MediaInstance{} = media_instance ->
        case Repo.delete(media_instance) do
          {:ok, _} ->
            if Repo.exists?(MediaInstance, file_id: media_instance.file_id) do
              Logger.info("MediaInstance deleted", instance_id: media_instance.id)
              {:ok, media_instance.id}
            else
              Repo.get_by(MediaFile, id: media_instance.file_id)
              |> delete_file
            end
          {:error, _} = err ->
            Logger.error("MediaInstance could not be deleted", instance_id: instance_id)
            err
        end
      nil ->
        Logger.error("MediaInstance not found for deletion", instance_id: instance_id)
        {:error, "MediaInstance not found"}
    end
  end

  @doc """
    Maps the deletion of all given instances
    @params: [%MediaInstance{}]
    @returns: [{status: "ok", instance_id} || {status: "error", reason: reason}]
  """
  def delete_all_instances(instances) do
    Enum.map(instances, fn %MediaInstance{} = instance ->
      case delete_single_instance(instance.id) do
        {:ok, instance_id} ->
          %{instance_id: instance_id, status: "ok"}
        {:error, reason} ->
          %{status: "error", reason: reason}
      end
    end)
  end

  @doc """
    Checks if a file is banned
    @params: binary hash
    @returns: boolean
  """
  def banned?(hash) do
    case Repo.get_by(ReportedFile, hash: hash) do
      nil -> false
      %ReportedFile{} -> true
    end
  end

  @doc """
    Retrieves all mediaFiles from a list of file_id's
    @params: [file_id uuid]
    @returns: [%MediaFile{}]
  """
  def retrieve_media_files_from_file_id(file_ids) do
    files = Enum.reduce(file_ids, [], fn id, acc ->
      case Repo.get_by(MediaFile, id: id) do
        %MediaFile{} = media_file ->
          [media_file | acc]
        nil ->
          Logger.warning("MediaFile not found for file_id", file_id: id)
          acc
      end
    end)
    %{files: files}
  end

  @doc """
    Patches all MediaFiles from given id's with the according content_flag
    ContentFlag isn't changed if an admin has already reviewed (approved) the file
    @params: content_flag @contentFlag, file_hash binary note String
    @returns: {file_id: uuid, status: "ok"} || {status: "error", reason: reason}
  """
  def update_content_flag(content_flag, file_hash, note) do
    binary_hash = Base.decode64!(file_hash)

    case Repo.get_by(MediaFile, hash: binary_hash) do
      nil ->
        Logger.error("MediaFile not found for content flag update", file_hash: file_hash)
        {:error, :not_found}

      %MediaFile{} = media_file ->
        media_file =
          if note == "Admin driven change" do
            %{media_file | admin_approved: true}
          else
            media_file
          end

        if media_file.admin_approved && note == "AI driven change" do
          Logger.info("MediaFile already approved by admin, skipping AI driven flag update", file_id: media_file.id)
          {:ok, media_file.id}
        else
          media_file
          |> Ecto.Changeset.change(content_flag: content_flag)
          |> Repo.update()
          |> case do
               {:ok, updated_file} ->
                 ReportService.update_reports(updated_file.id)
                 Logger.info("MediaFile content flag updated", file_id: updated_file.id, content_flag: content_flag, note: note)
                 {:ok, updated_file.id}

               {:error, reason} ->
                 Logger.error("MediaFile content flag could not be updated", file_id: media_file.id, content_flag: content_flag, note: note, reason: reason)
                 {:error, reason}
             end
        end
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
      content_flag: MediaFile.content_flag("UNASSIGNED")
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