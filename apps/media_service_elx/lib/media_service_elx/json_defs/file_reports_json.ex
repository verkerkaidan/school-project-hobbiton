defmodule MediaServiceElxWeb.ReportJSON do
  @moduledoc """
This file handles how the data is shown to the user
e.g. what fields are shows when an database entry is returned from a request
"""

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