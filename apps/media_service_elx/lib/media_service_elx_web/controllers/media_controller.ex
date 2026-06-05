defmodule MediaServiceElxWeb.MediaController do
  @moduledoc """
Home of the first functions that are called when a request is coming in from api/media
"""
  @compile {:no_warn_undefined, [
    MediaServiceElx.MediaServiceMock,
    MediaServiceElx.ReportServiceMock
  ]}

  use MediaServiceElxWeb, :controller

  defp media_service do
    Application.get_env(:media_service_elx, :media_service, MediaServiceElx.MediaService)
  end
  defp report_service do
    Application.get_env(:media_service_elx, :report_service, MediaServiceElx.ReportService)
  end

  alias MediaServiceElx.MediaFiles.MediaFile

  @doc """
  Handles the uploading of one or multiple media files by the same owner
  @params: http connection, uuid, form-data file
  @returns: {:error, :banned} or {:id, uuid}
  """
  def upload_media(conn, %{"owner_id" => owner_id, "files" => uploads}) when is_list(uploads) do
    results = Enum.map(uploads, fn upload ->
      case media_service().upload_media_file(upload, owner_id) do
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
    IO.inspect(instance_ids, label: "instance ids at upload")

    results = Enum.map(instance_ids, fn instance_id ->
      case media_service().retrieve_media_file(instance_id) do
        {:ok, %MediaFile{} = media_file} ->
          %{relevantId: instance_id, fileData: Base.encode64(media_file.original), contentType: media_file.content_type, contentFlag: media_file.content_flag}
        {:error, _} ->
          nil
      end
    end)

    json(conn, results)
  end

  @doc """
  Deletes a single instance from an instance_id
  @params: connection, uuid
  @returns: {id: uuid} or {error: :error}
  """
  def delete_instance(conn, %{"instance_id" => instance_id}) do
    case media_service().delete_single_instance(instance_id) do
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
  @params: [{fileId: uuid, contentFlag: @contentFlag, note: String}]
  @returns: {results: [{:ok, uuid}]}
  """
  def update_content_flag(conn, %{"_json" => updates}) when is_list(updates) do
    IO.inspect(updates, label: "incoming update")
    results = Enum.map(updates, fn update ->
      content_flag = Map.fetch!(update, "contentFlag")
      file_hash    = Map.fetch!(update, "hash")
      note         = Map.get(update, "note", "")

      flag = MediaFile.content_flag(content_flag)
      case media_service().update_content_flag(flag, file_hash, note) do
        {:ok, id}         -> %{file_id: id, status: "ok"}
        {:error, reason}  -> %{status: "error", reason: reason}
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
    result = media_service().retrieve_unassigned()
    |> Enum.map(fn %MediaFile{} = media_file ->
      %{file: Base.encode64(media_file.original), hash: Base.encode64(media_file.hash)}
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
  def delete_media_file(conn, %{"hash" => hash}) do
    case report_service().ban_file(hash) do
      {:ok, file_id} ->
        json(conn, %{id: file_id})
      {:error, _reason} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: "Invalid id"})
    end
  end
end