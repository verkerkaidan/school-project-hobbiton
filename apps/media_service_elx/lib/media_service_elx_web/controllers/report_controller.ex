defmodule MediaServiceElxWeb.ReportController do
  use MediaServiceElxWeb, :controller

  alias MediaServiceElx.FileReports
  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.FileReports.ReportedFile

  ## TODO: Now creates a report using the instance_id and not the file_id
  def create_file_report(conn, %{"user_id" => user_id, "instance_id" => instance_id} = params) do
    report_params = %{
      "user_id" => user_id,
      "file_id" => instance_id
    }
    with {:ok, %FileReport{} = file_report} <- FileReports.create_file_report(report_params) do
      conn
      |> put_status(:created)
      |> render(:show, report: file_report)
    else
      {:error, %Ecto.Changeset{} = changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> render(:error, changeset: changeset)
    end
  end

  def get_all_reported_files(conn, _params) do

  end
end