defmodule MediaServiceElx.ReportService do
  @moduledoc """
    Handles most logic regarding actions with file reports and reported files
  """
  @behaviour MediaServiceElx.ReportServiceBehaviour

  require Logger

  import Ecto.Query

  alias MediaServiceElx.Repo
  alias MediaServiceElx.FileReports.ReportedFile
  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaService

  @doc """
    Creates a file report for given user and file
    @params: user_id uuid, %MediaFile{}
    @returns:
  """
  def create_file_report(user_id, %MediaFile{} = media_file) do
    %FileReport{}
    |> FileReport.changeset(%{
      file_id: media_file.id,
      user_id: user_id
    })
    |> Repo.insert()
    |> case do
         {:ok, %FileReport{} = file_report} ->
           Logger.info("File report created", user: user_id, file_id: media_file.id, report_id: file_report.id)
           {:ok, file_report}
         {:error, reason} ->
           Logger.error("File report could not be created", user: user_id, file_id: media_file.id, reason: reason)
           {:error, reason}
       end
  end

  @doc """
  Handles the banning of media files
  @params: hash of the media file to be banned
  @returns: {:error, :unprocessable_entity} or {:ok, uuid}
  """
  def ban_file(hash) do
    binary_hash = Base.decode64!(hash)

    case Repo.get_by(MediaFile, hash: binary_hash) do
      %MediaFile{} = media_file ->
        if MediaService.banned?(media_file.hash) do
          Logger.info("MediaFile is already banned", file_id: media_file.id)
          {:ok, media_file.id}
        else
          case create_reported_file(media_file) do
            {:error, reason} ->
              Logger.error("MediaFile could not be banned", file_id: media_file.id, reason: reason)
              {:error, :unprocessable_entity}
            {:ok, %ReportedFile{} = _reported_file} ->
              Logger.info("MediaFile banned", file_id: media_file.id)
              MediaService.delete_file(media_file)
          end
        end
      nil ->
        Logger.error("MediaFile not found for ban", hash: hash)
        {:error, :unprocessable_entity}
    end
  end

  @doc """
    Removes a single report
    @params report_id uuid
    @returns {:error, :unprocessable_entity} or {:ok, uuid}
  """
  def delete_single_report(report_id) do
    case Repo.get_by(FileReport, id: report_id) do
      %FileReport{} = file_report ->
        case Repo.delete(file_report) do
          {:ok, _} ->
            Logger.info("File report deleted", report_id: file_report.id)
            {:ok, file_report.id}
          {:error, reason} = err ->
            Logger.error("File report could not be deleted", report_id: report_id, reason: reason)
            err
        end
      nil ->
        Logger.error("FileReport not found for deletion", report_id: report_id)
        {:error, "FileReport not found"}
    end
  end

  @doc """
    Handles the removal of all given reports
    @params: [%FileReport{}]
    @returns: [{status: "ok", report_id} || {status: "error", reason: reason}]
  """
  def remove_all_reports(reports) do
    Enum.map(reports, fn %FileReport{} = report ->
      case delete_single_report(report.id) do
        {:ok, report_id} ->
          %{report_id: report_id, status: "ok"}
        {:error, reason} ->
          %{status: "error", reason: reason}
      end
    end)
  end

  @doc """
    Retrieves all reports from a given owner
    @params owner_id uuid
    @returns
  """
  def retrieve_reports_by_owner(owner_id) do
    Repo.all_by(FileReport, user_id: owner_id)
  end

  @doc """
    Retrieves all reports
    @params: --
    @returns: [file_id uuid]
  """
  def retrieve_all_unique_reported_files() do
    from(report in FileReport,
      distinct: report.file_id,
      select: report.file_id
    )
    |> Repo.all()
  end

  @doc """
    Updates all reports from a given file_id
    @params: file_id uuid
    @returns: --
  """
  # TODO -- Notify users
  def update_reports(file_id) do
    Repo.all_by(FileReport, file_id: file_id)
    |> Enum.map(fn %FileReport{} = report ->
      case Repo.delete(report) do
        {:ok, _} ->
          Logger.info("File report removed after content flag update", report_id: report.id, file_id: file_id)
        {:error, reason} ->
          Logger.error("File report could not be removed after content flag update", report_id: report.id, file_id: file_id, reason: reason)
      end
    end)
  end

  #  @doc """
  #  Generates a ReportedFile
  #  @params: MediaFile object
  #  @returns: {:ok, ReportedFile}
  #  """
  defp create_reported_file(%MediaFile{} = media_file) do
    %ReportedFile{}
    |> ReportedFile.changeset(%{
      hash: media_file.hash
    })
    |> Repo.insert()
  end
end