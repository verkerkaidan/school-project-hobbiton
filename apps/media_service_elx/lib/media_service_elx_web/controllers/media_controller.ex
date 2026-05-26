defmodule MediaServiceElxWeb.MediaController do
  @moduledoc """
Home of the first functions that are called when a request is coming in from api/media
"""

  use MediaServiceElxWeb, :controller

  alias MediaServiceElx.MediaService
  alias MediaServiceElx.ReportService
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElxWeb.MediaFilesJSON

  @doc """
  Handles the uploading of one or multiple media files by the same owner
  @params: http connection, uuid, form-data file
  @returns: {:error, :banned} or {:id, uuid}
  """
  def upload_media(conn, %{"owner_id" => owner_id, "files" => uploads}) when is_list(uploads) do
    results = Enum.map(uploads, fn upload ->
      case MediaService.upload_media_file(upload, owner_id) do
        {:ok, instance_id} -> %{id: instance_id, status: "ok"}
        {:error, :banned}  -> %{status: "error", reason: "banned"}
        {:error, _reason}  -> %{status: "error", reason: "invalid file"}
      end
    end)

    json(conn, %{results: results})
  end

  @doc """
  Retrieves media files from an instance_id
  @params: List of instance_id's
  @returns: List of partial MediaFile's
  """
  def retrieve_media(conn, %{"instances" => instance_ids}) when is_list(instance_ids) do
    results = Enum.map(instance_ids, fn instance_id ->
      case MediaService.retrieve_media_file(instance_id) do
        {:ok, %MediaFile{} = media_file} ->
          %{instance_id: instance_id, status: "ok", media_file: MediaFilesJSON.partial_file_show(%{file: media_file}).data}
        {:error, reason} ->
          %{status: "error", reason: reason}
      end
    end)

    json(conn, %{results: results})
  end

  @doc """
  Deletes a single instance from an instance_id
  @params: connection, uuid
  @returns: {id: uuid} or {error: :error}
  """
  def delete_instance(conn, %{"instance_id" => instance_id}) do
    case MediaService.delete_single_instance(instance_id) do
      {:ok, _id} ->
        json(conn, %{id: instance_id})
      {:error, reason} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: reason})
    end
  end

  @doc """
  Updates the content_flag of MediaFile for each of the ids
  @params: [{fileId: uuid, contentFlag: @contentFlag}]
  @returns: {results: {:ok, uuid}}
  """
  def update_content_flag(conn, %{"_json" => updates}) when is_list(updates) do
    results = Enum.map(updates, fn %{"contentFlag" => content_flag, "id" => file_id} ->
      flag = MediaFile.content_flag(content_flag)
      case MediaService.update_content_flag(flag, file_id) do
        {:ok, id} -> %{file_id: id, status: "ok"}
        {:error, reason} -> %{status: "error", reason: reason}
      end
    end)

    json(conn, %{results: results})
  end

  @doc """
  Retrieves all MediaFile where content_flag == unassigned
  @params: --
  @returns: [{id: uuid, file: base64}]
  """
  def get_unassigned_media_files(conn, _params) do
    result = MediaService.retrieve_unassigned()
    |> Enum.map(fn %MediaFile{} = media_file ->
      %{file: Base.encode64(media_file.original), id: media_file.id}
    end)

    json(conn, result)
  end

  @doc """
  Retrieves complete MediaFile from an instance_id for admin inspection
  @params:
  @returns:
  """
  def get_complete_media_file(_conn, _params) do

  end

  @doc """
  Deletes and bans a MediaFile
  @params: connection, uuid
  @returns: {id: uuid}
  """
  def delete_media_file(conn, %{"file_id" => file_id}) do
    case ReportService.ban_file(file_id) do
      {:ok, file_id} ->
        json(conn, %{id: file_id})
      {:error, _reason} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: "Invalid id"})
    end
  end
end