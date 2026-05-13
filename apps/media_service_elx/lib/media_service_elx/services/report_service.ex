defmodule MediaServiceElx.ReportService do
  @moduledoc """
  Handles most logic regarding actions with file reports and reported files
"""

  alias MediaServiceElx.Repo
  alias MediaServiceElx.FileReports.ReportedFile
  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaService

  @doc """
  Handles the banning of media files
  @params: uuid
  @returns: {:error, :unprocessable_entity} or {:ok, uuid}
  """
  def ban_file(file_id) do
    case Repo.get_by(MediaFile, id: file_id) do
      %MediaFile{} = media_file ->
        if MediaService.banned?(media_file.hash) do
          {:ok, media_file.id}
        else
          case create_reported_file(media_file) do
            {:error, _reason} ->
              {:error, :unprocessable_entity}
            {:ok, %ReportedFile{} = _reported_file} ->
              MediaService.delete_file(media_file)
          end
        end
      nil ->
        {:error, :unprocessable_entity}
    end
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