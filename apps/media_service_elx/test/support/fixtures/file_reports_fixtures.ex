defmodule MediaServiceElx.FileReportsFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.FileReports` context.
  """

  @doc """
  Generate a file_report.
  """
  def file_report_fixture(attrs \\ %{}) do
    {:ok, file_report} =
      attrs
      |> Enum.into(%{
        file_id: "some file_id",
        user_id: "some user_id"
      })
      |> MediaServiceElx.FileReports.create_file_report()

    file_report
  end

  @doc """
  Generate a reported_file.
  """
  def reported_file_fixture(attrs \\ %{}) do
    {:ok, reported_file} =
      attrs
      |> Enum.into(%{
        hash: "some hash"
      })
      |> MediaServiceElx.FileReports.create_reported_file()

    reported_file
  end
end
