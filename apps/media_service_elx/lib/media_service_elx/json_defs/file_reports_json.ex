defmodule MediaServiceElxWeb.ReportJSON do
  alias MediaServiceElx.FileReports.FileReport

  def index(%{reports: reports}) do
    %{data: for(report <- reports, do: data(report))}
  end

  def show(%{report: report}) do
    %{data: data(report)}
  end

  defp data(%FileReport{} = report) do
    %{
      id: report.id,
      file_id: report.file_id,
      user_id: report.user_id
    }
  end
end