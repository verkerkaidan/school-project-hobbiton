defmodule MediaServiceElx.FileReportsFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.FileReports` context.
  """

  alias MediaServiceElx.FileReports

  # ---------------------------------------------------------------------------
  # Test helpers / fixtures
  # ---------------------------------------------------------------------------

  def valid_file_report_attrs do
    %{
      file_id: Ecto.UUID.generate(),
      user_id: Ecto.UUID.generate()
    }
  end

  def valid_reported_file_attrs do
    %{hash: :crypto.strong_rand_bytes(32)}
  end

  def insert_file_report(attrs \\ %{}) do
    {:ok, file_report} =
      valid_file_report_attrs()
      |> Map.merge(attrs)
      |> then(&FileReports.create_file_report/1)

    file_report
  end

  def insert_reported_file(attrs \\ %{}) do
    {:ok, reported_file} =
      valid_reported_file_attrs()
      |> Map.merge(attrs)
      |> then(&FileReports.create_reported_file/1)

    reported_file
  end
end
