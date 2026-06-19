defmodule MediaServiceElxWeb.ReportController do
  @compile {:no_warn_undefined, [
    MediaServiceElx.MediaServiceMock,
    MediaServiceElx.ReportServiceMock
  ]}

  use MediaServiceElxWeb, :controller
  require Logger

  @media_service Application.get_env(:media_service_elx, :media_service, MediaServiceElx.MediaService)

  @report_service Application.get_env(:media_service_elx, :report_service, MediaServiceElx.ReportService)

  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElxWeb.MediaFilesJSON

  @doc """
    Creates a file report from a given user and instance
    @params: user_id uuid, instance_id uuid
    @returns: {:ok, report_id: uuid}
  """
  def create_file_report(conn, %{"user_id" => user_id, "instance_id" => instance_id} = _params) do
    case @media_service.retrieve_media_file(instance_id) do
      {:ok, %MediaFile{} = media_file} ->
        case @report_service.create_file_report(user_id, media_file) do
          {:ok, %FileReport{} = file_report} ->
            Logger.info("File report created", user: user_id, instance_id: instance_id, report_id: file_report.id)
            json(conn, %{report_id: file_report.id, status: "ok"})
          {:error, reason} ->
            Logger.error("File report could not be created", user: user_id, instance_id: instance_id, reason: reason)
            json(conn, %{status: "error", reason: reason})
        end
      {:error, reason} ->
        Logger.error("MediaFile not found for report creation", user: user_id, instance_id: instance_id, reason: reason)
        json(conn, %{status: "error", reason: "MediaFile not found"})
    end
  end

  @doc """
    Deletes a single given report
    @params: report_id uuid
    @returns: {:ok, uuid} || {error, "reason"}
  """
  def delete_single_report(conn, %{"report_id" => report_id}) do
    case @report_service.delete_single_report(report_id) do
      {:ok, report_id} ->
        Logger.info("Report deleted", report_id: report_id)
        json(conn, %{status: "ok", report_id: report_id})
      {:error, reason} ->
        Logger.error("Report could not be deleted", report_id: report_id, reason: reason)
        json(conn, %{status: "error", reason: reason})
    end
  end

  @doc """
    Returns all reported files
    @params: --
    @returns: [%MediaFile{}]
  """
  def get_all_reported_files(conn, _params) do
    result = @report_service.retrieve_all_unique_reported_files()
             |> @media_service.retrieve_media_files_from_file_id
             |> MediaFilesJSON.full_file_index

    Logger.info("Reported files retrieved", results: result.data)

    json(conn, result.data)
  end
end