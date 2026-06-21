defmodule MediaServiceElxWeb.MediaController do
  @moduledoc """
Home of the first functions that are called when a request is coming in from api/media
"""
  @compile {:no_warn_undefined, [
    MediaServiceElx.MediaServiceMock,
    MediaServiceElx.ReportServiceMock
  ]}

  use MediaServiceElxWeb, :controller
  require Logger

  @media_service Application.get_env(:media_service_elx, :media_service, MediaServiceElx.MediaService)

  @report_service Application.get_env(:media_service_elx, :report_service, MediaServiceElx.ReportService)

  alias MediaServiceElx.MediaFiles.MediaFile

  @doc """
  Handles the uploading of one or multiple media files by the same owner
  @params: http connection, uuid, form-data file
  @returns: {:error, :banned} or {:id, uuid}
  """
  def upload_media(conn, %{"owner_id" => owner_id, "files" => uploads}) when is_list(uploads) do
    results = Enum.map(uploads, fn upload ->
      case @media_service.upload_media_file(upload, owner_id) do
        {:ok, instance_id} ->
          Logger.info("Media uploaded", user: owner_id)
          %{id: instance_id, status: "ok"}
        {:error, :banned}  ->
          Logger.warning("Tried to upload a banned file", user: owner_id)
          %{status: "error", reason: "banned"}
        {:error, _reason}  ->
          Logger.error("File could not be uploaded", user: owner_id, upload: upload)
          %{status: "error", reason: "invalid file"}
      end
    end)

    Logger.info("Media uploaded", results: results)

    json(conn, %{results: results})
  end

  @doc """
  Retrieves media files from an instance_id
  @params: List of instance_id's
  @returns: List of partial MediaFile's
  """
  def retrieve_media(conn, %{"instances" => instance_ids}) when is_list(instance_ids) do
    results = Enum.map(instance_ids, fn instance_id ->
      case @media_service.retrieve_media_file(instance_id) do
        {:ok, %MediaFile{} = media_file} ->
          %{relevantId: instance_id, fileData: Base.encode64(media_file.original), contentType: media_file.content_type, contentFlag: media_file.content_flag}
        {:error, reason} ->
          Logger.error("Media file could not be retrieved", reason: reason)
          nil
      end
    end)
    |> Enum.reject(&is_nil/1)

    Logger.info("Media retrieved", results: results)

    json(conn, results)
  end

  @doc """
  Deletes a single instance from an instance_id
  @params: connection, uuid
  @returns: {id: uuid} or {error: :error}
  """
  def delete_instances(conn, %{"instances" => instance_ids}) when is_list(instance_ids) do
    results = Enum.map(instance_ids, fn instance_id ->
      case @media_service.delete_single_instance(instance_id) do
        {:ok, _id} ->
          %{id: instance_id}
        {:error, reason} ->
          Logger.error("MediaInstance could not be deleted", id: instance_id, reason: reason)
          nil
      end
    end)
    |> Enum.reject(&is_nil/1)

    json(conn, results)
  end

  @doc """
  Updates the content_flag of MediaFile for each of the ids
  @params: [{fileId: uuid, contentFlag: @contentFlag, note: String}]
  @returns: {results: [{:ok, uuid}]}
  """
  def update_content_flag(conn, %{"_json" => updates}) when is_list(updates) do
    results = Enum.map(updates, fn update ->
      content_flag = Map.fetch!(update, "contentFlag")
      file_hash    = Map.fetch!(update, "hash")
      note         = Map.get(update, "note", "")

      flag = MediaFile.content_flag(content_flag)
      case @media_service.update_content_flag(flag, file_hash, note) do
        {:ok, id}         ->
          Logger.info("ContentFlag updated", file_hash: file_hash, note: note, flag: flag)
          %{file_id: id, status: "ok"}
        {:error, reason}  ->
          Logger.error("ContentFlag could not be updated", file_hash: file_hash, note: note, flag: flag, reason: reason)
          %{status: "error", reason: reason}
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
    result = @media_service.retrieve_unassigned()
    |> Enum.map(fn %MediaFile{} = media_file ->
      %{file: Base.encode64(media_file.original), hash: Base.encode64(media_file.hash)}
    end)

    Logger.info("Unassigned files retrieved", results: result)

    json(conn, result)
  end

  @doc """
  Deletes and bans a MediaFile
  @params: connection, uuid
  @returns: {id: uuid}
  """
  def delete_media_file(conn, %{"hash" => hash}) do
    if "ROLE_ADMIN" in conn.assigns.current_user.roles do
      case @report_service.ban_file(hash) do
        {:ok, file_id} ->
          Logger.info("A mediafile was deleted and banned")
          json(conn, %{id: file_id})
        {:error, reason} ->
          Logger.error("Deletion of mediafile not successful", reason: reason, hash: hash)
          conn
          |> put_status(:unprocessable_entity)
          |> json(%{error: "Invalid id"})
      end
    else
      Logger.warning("Unauthorised request to delete_media_file")
      conn |> put_status(:forbidden) |> json(%{error: "admin only"}) |> halt()
    end
  end
  def delete_media_file(conn, _params) do
    Logger.warning("Request to delete_media_file with missing hash")
    conn |> put_status(:bad_request) |> json(%{error: "hash is required"})
  end
end