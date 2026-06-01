defmodule MediaServiceElxWeb.ReportController do
  use MediaServiceElxWeb, :controller

  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaService
  alias MediaServiceElx.ReportService
  alias MediaServiceElxWeb.MediaFilesJSON

  @doc """
    Creates a file report from a given user and instance
    @params: user_id uuid, instance_id uuid
    @returns: {:ok, report_id: uuid}
  """
  def create_file_report(conn, %{"user_id" => user_id, "instance_id" => instance_id} = _params) do
    case MediaService.retrieve_media_file(instance_id) do
      {:ok, %MediaFile{} = media_file} ->
        case ReportService.create_file_report(user_id, media_file) do
          {:ok, %FileReport{} = file_report} ->
            json(conn, %{report_id: file_report.id, status: "ok"})
          {:error, reason} ->
            json(conn, %{status: "error", reason: reason})
        end
      {:error, _} ->
        json(conn, %{status: "error", reason: "MediaFile not found"})
    end
  end

  @doc """
    Deletes a single given report
    @params: report_id uuid
    @returns: {:ok, uuid} || {error, "reason"}
  """
  def delete_single_report(conn, %{"report_id" => report_id}) do
    case ReportService.delete_single_report(report_id) do
      {:ok, report_id} -> json(conn, %{status: "ok", report_id: report_id})
      {:error, reason} -> json(conn, %{status: "error", reason: reason})
    end
  end

  @doc """
    Returns all reported files
    @params: --
    @returns: [%MediaFile{}]
  """
  def get_all_reported_files(conn, _params) do
    result = ReportService.retrieve_all_unique_reported_files()
    |> MediaService.retrieve_media_files_from_file_id
    |> MediaFilesJSON.full_file_index

    json(conn, result)
  end
end