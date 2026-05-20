defmodule MediaServiceElxWeb.UserController do
  @moduledoc """
  Home of the first functions that are called when a request is coming in from api/user
  """

  use MediaServiceElxWeb, :controller

  alias MediaServiceElx.ReportService
  alias MediaServiceElx.MediaService
  alias MediaServiceElx.MediaInstances.MediaInstance
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElxWeb.MediaFilesJSON

  @doc """
  Retrieves all media files from an owner_id
  @params: owner_id uuid
  @returns: {results: [%PartialMediaFile{}]}
  """
  def retrieve_media_by_owner(conn, %{"owner_id" => owner_id}) do
    instances = MediaService.retrieve_instances_by_owner(owner_id)

    results = Enum.map(instances, fn %MediaInstance{} = instance ->
      case MediaService.retrieve_media_file(instance.id) do
        {:ok, %MediaFile{} = media_file} ->
          %{instance_id: instance.id, status: "ok", media_file: MediaFilesJSON.partial_file_show(%{file: media_file}).data}
        {:error, reason} ->
          %{status: "error", reason: reason}
      end
    end)

    json(conn, %{results: results})
  end

  @doc """
  Deletes all MediaInstance from the same owner
  @params: owner_id uuid
  @returns: {results: {instances: [{:ok, uuid}], reports: [{:ok, uuid}]}}
  """
  def delete_owner(conn, %{"owner_id" => owner_id}) do
    instances = MediaService.retrieve_instances_by_owner(owner_id)
    instance_results = MediaService.delete_all_instances(instances)

    reports = ReportService.retrieve_reports_by_owner(owner_id)
    report_results = ReportService.remove_all_reports(reports)

    json(conn, %{results: %{instances: instance_results, reports: report_results}})
  end
end
