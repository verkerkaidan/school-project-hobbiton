defmodule MediaServiceElx.ReportServiceBehaviour do
  @moduledoc """
  Behaviour contract for ReportService.
  Allows Mox to mock this module in tests.
  """

  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.FileReports.FileReport

  @doc """
  Creates a file report for a given user and file.
  """
  @callback create_file_report(user_id :: String.t(), media_file :: MediaFile.t()) ::
              {:ok, FileReport.t()} | {:error, Ecto.Changeset.t()}

  @doc """
  Bans a media file by its id, creating a ReportedFile record and deleting the original.
  """
  @callback ban_file(file_id :: String.t()) ::
              {:ok, String.t()} | {:error, atom()}

  @doc """
  Deletes a single file report by its id.
  """
  @callback delete_single_report(report_id :: String.t()) ::
              {:ok, String.t()} | {:error, String.t() | atom()}

  @doc """
  Removes all given reports, returning a status map for each.
  """
  @callback remove_all_reports(reports :: [FileReport.t()]) ::
              list(%{report_id: String.t(), status: String.t()} | %{status: String.t(), reason: any()})

  @doc """
  Retrieves all reports filed by a given owner.
  """
  @callback retrieve_reports_by_owner(owner_id :: String.t()) :: [FileReport.t()]

  @doc """
  Retrieves all unique file ids that have been reported.
  """
  @callback retrieve_all_unique_reported_files() :: [String.t()]

  @doc """
  Deletes all reports associated with a given file_id.
  """
  @callback update_reports(file_id :: String.t()) :: list()
end