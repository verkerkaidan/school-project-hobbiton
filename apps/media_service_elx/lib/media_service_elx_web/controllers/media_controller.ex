defmodule MediaServiceElxWeb.MediaController do
  @moduledoc """
Home of the first functions that are called when a request is coming in from api/media
"""

  use MediaServiceElxWeb, :controller

  alias MediaServiceElx.MediaService
  alias MediaServiceElx.ReportService

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
  @params:
  @returns:
  """
  def retrieve_media(conn, _params) do

  end

  @doc """
  Retrieves all media files from an owner_id
  @params:
  @returns:
  """
  def retrieve_media_by_owner(conn, _params) do

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
  Deletes all MediaInstance from the same owner
  @params:
  @returns:
  """
  def delete_owner(conn, _params) do

  end

  @doc """
  Updates the content_flag of MediaFile for each of the ids
  @params:
  @returns:
  """
  def update_content_flag(conn, _params) do

  end

  @doc """
  Retrieves all MediaFile where content_flag == unassigned
  @params:
  @returns:
  """
  def get_unassigned_media_files(conn, _params) do

  end

  @doc """
  Retrieves complete MediaFile from an instance_id
  @params:
  @returns:
  """
  def get_complete_media_file(conn, _params) do

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
      {:error, reason} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: "Invalid id"})
    end
  end
end