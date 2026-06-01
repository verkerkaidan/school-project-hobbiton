defmodule MediaServiceElx.FileReports do
  @moduledoc """
  The FileReports context.
  """

  import Ecto.Query, warn: false
  alias MediaServiceElx.Repo

  alias MediaServiceElx.FileReports.FileReport

  @doc """
  Returns the list of file_report.

  ## Examples

      iex> list_file_report()
      [%FileReport{}, ...]

  """
  def list_file_report do
    Repo.all(FileReport)
  end

  @doc """
  Gets a single file_report.

  Raises `Ecto.NoResultsError` if the File report does not exist.

  ## Examples

      iex> get_file_report!(123)
      %FileReport{}

      iex> get_file_report!(456)
      ** (Ecto.NoResultsError)

  """
  def get_file_report!(id), do: Repo.get!(FileReport, id)

  @doc """
  Creates a file_report.

  ## Examples

      iex> create_file_report(%{field: value})
      {:ok, %FileReport{}}

      iex> create_file_report(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_file_report(attrs) do
    %FileReport{}
    |> FileReport.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a file_report.

  ## Examples

      iex> update_file_report(file_report, %{field: new_value})
      {:ok, %FileReport{}}

      iex> update_file_report(file_report, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_file_report(%FileReport{} = file_report, attrs) do
    file_report
    |> FileReport.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a file_report.

  ## Examples

      iex> delete_file_report(file_report)
      {:ok, %FileReport{}}

      iex> delete_file_report(file_report)
      {:error, %Ecto.Changeset{}}

  """
  def delete_file_report(%FileReport{} = file_report) do
    Repo.delete(file_report)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking file_report changes.

  ## Examples

      iex> change_file_report(file_report)
      %Ecto.Changeset{data: %FileReport{}}

  """
  def change_file_report(%FileReport{} = file_report, attrs \\ %{}) do
    FileReport.changeset(file_report, attrs)
  end

  alias MediaServiceElx.FileReports.ReportedFile

  @doc """
  Returns the list of reported_file.

  ## Examples

      iex> list_reported_file()
      [%ReportedFile{}, ...]

  """
  def list_reported_file do
    Repo.all(ReportedFile)
  end

  @doc """
  Gets a single reported_file.

  Raises `Ecto.NoResultsError` if the Reported file does not exist.

  ## Examples

      iex> get_reported_file!(123)
      %ReportedFile{}

      iex> get_reported_file!(456)
      ** (Ecto.NoResultsError)

  """
  def get_reported_file!(id), do: Repo.get!(ReportedFile, id)

  @doc """
  Creates a reported_file.

  ## Examples

      iex> create_reported_file(%{field: value})
      {:ok, %ReportedFile{}}

      iex> create_reported_file(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_reported_file(attrs) do
    %ReportedFile{}
    |> ReportedFile.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a reported_file.

  ## Examples

      iex> update_reported_file(reported_file, %{field: new_value})
      {:ok, %ReportedFile{}}

      iex> update_reported_file(reported_file, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_reported_file(%ReportedFile{} = reported_file, attrs) do
    reported_file
    |> ReportedFile.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a reported_file.

  ## Examples

      iex> delete_reported_file(reported_file)
      {:ok, %ReportedFile{}}

      iex> delete_reported_file(reported_file)
      {:error, %Ecto.Changeset{}}

  """
  def delete_reported_file(%ReportedFile{} = reported_file) do
    Repo.delete(reported_file)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking reported_file changes.

  ## Examples

      iex> change_reported_file(reported_file)
      %Ecto.Changeset{data: %ReportedFile{}}

  """
  def change_reported_file(%ReportedFile{} = reported_file, attrs \\ %{}) do
    ReportedFile.changeset(reported_file, attrs)
  end
end
