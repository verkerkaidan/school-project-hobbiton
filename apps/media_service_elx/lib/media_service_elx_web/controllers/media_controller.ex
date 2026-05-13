defmodule MediaServiceElxWeb.MediaController do
  use MediaServiceElxWeb, :controller

  alias MediaServiceElx.MediaService
  alias MediaServiceElx.ReportService

  def upload_media(conn, %{"owner_id" => owner_id, "file" => upload}) do
    case MediaService.upload_media_file(upload, owner_id) do
      {:ok, instance_id} ->
        json(conn, %{id: instance_id})
      {:error, :banned} ->
        conn
        |> put_status(:forbidden)
        |> json(%{error: "This file was banned from this platform"})
      {:error, reason} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{error: "Invalid file"})
    end
  end

  def retrieve_media(conn, _params) do

  end

  def retrieve_media_by_owner(conn, _params) do

  end

  def delete_instance(conn, _params) do

  end

  def delete_owner(conn, _params) do

  end

  def update_content_flag(conn, _params) do

  end

  def get_unassigned_media_files(conn, _params) do

  end

  def get_complete_media_file(conn, _params) do

  end

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