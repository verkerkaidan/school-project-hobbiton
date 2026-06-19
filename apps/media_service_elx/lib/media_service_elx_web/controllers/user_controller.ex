defmodule MediaServiceElxWeb.UserController do
  @moduledoc """
  Home of the first functions that are called when a request is coming in from api/user
  """
  @compile {:no_warn_undefined, [
    MediaServiceElx.MediaServiceMock,
    MediaServiceElx.ReportServiceMock
  ]}

  use MediaServiceElxWeb, :controller
  require Logger

  @media_service Application.get_env(:media_service_elx, :media_service, MediaServiceElx.MediaService)

  @report_service Application.get_env(:media_service_elx, :report_service, MediaServiceElx.ReportService)

  alias MediaServiceElx.MediaInstances.MediaInstance
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElxWeb.MediaFilesJSON

  @doc """
  Retrieves all media files from an owner_id
  @params: owner_id uuid
  @returns: {results: [%PartialMediaFile{}]}
  """
  def retrieve_media_by_owner(conn, %{"owner_id" => owner_id}) do
    instances = @media_service.retrieve_instances_by_owner(owner_id)

    results = Enum.map(instances, fn %MediaInstance{} = instance ->
      case @media_service.retrieve_media_file(instance.id) do
        {:ok, %MediaFile{} = media_file} ->
          %{instance_id: instance.id, status: "ok", media_file: MediaFilesJSON.partial_file_show(%{file: media_file}).data}
        {:error, reason} ->
          Logger.error("Media file could not be retrieved for instance", instance_id: instance.id, owner: owner_id, reason: reason)
          %{status: "error", reason: reason}
      end
    end)

    Logger.info("Media retrieved by owner", owner: owner_id, results: results)

    json(conn, %{results: results})
  end

  @doc """
  Deletes all MediaInstance from the same owner
  @params: owner_id uuid
  @returns: {results: {instances: [{:ok, uuid}], reports: [{:ok, uuid}]}}
  """
  def delete_owner(conn, %{"owner_id" => owner_id}) do
    instances = @media_service.retrieve_instances_by_owner(owner_id)
    instance_results = @media_service.delete_all_instances(instances)

    reports = @report_service.retrieve_reports_by_owner(owner_id)
    report_results = @report_service.remove_all_reports(reports)

    Logger.info("Owner deleted", owner: owner_id, instances: instance_results, reports: report_results)

    json(conn, %{instances: instance_results, reports: report_results})
  end

  def retrieve_reports_by_owner(conn, %{"owner_id" => owner_id}) do
    reports = @report_service.retrieve_reports_by_owner(owner_id)
              |> Enum.map(fn report -> %{id: report.id, file_id: report.file_id, user_id: report.user_id} end)

    Logger.info("Reports retrieved by owner", owner: owner_id, results: reports)

    json(conn, %{reports: reports})
  end
end