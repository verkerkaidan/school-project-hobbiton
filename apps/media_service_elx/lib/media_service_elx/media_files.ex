defmodule MediaServiceElx.MediaFiles do
  @moduledoc """
  The MediaFiles context.
  """

  import Ecto.Query, warn: false
  alias MediaServiceElx.Repo

  alias MediaServiceElx.MediaFiles.MediaFile

  @doc """
  Returns the list of media_files.

  ## Examples

      iex> list_media_files()
      [%MediaFile{}, ...]

  """
  def list_media_files do
    Repo.all(MediaFile)
  end

  @doc """
  Gets a single media_file.

  Raises `Ecto.NoResultsError` if the Media file does not exist.

  ## Examples

      iex> get_media_file!(123)
      %MediaFile{}

      iex> get_media_file!(456)
      ** (Ecto.NoResultsError)

  """
  def get_media_file!(id), do: Repo.get!(MediaFile, id)

  @doc """
  Creates a media_file.

  ## Examples

      iex> create_media_file(%{field: value})
      {:ok, %MediaFile{}}

      iex> create_media_file(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_media_file(attrs) do
    %MediaFile{}
    |> MediaFile.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a media_file.

  ## Examples

      iex> update_media_file(media_file, %{field: new_value})
      {:ok, %MediaFile{}}

      iex> update_media_file(media_file, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_media_file(%MediaFile{} = media_file, attrs) do
    media_file
    |> MediaFile.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a media_file.

  ## Examples

      iex> delete_media_file(media_file)
      {:ok, %MediaFile{}}

      iex> delete_media_file(media_file)
      {:error, %Ecto.Changeset{}}

  """
  def delete_media_file(%MediaFile{} = media_file) do
    Repo.delete(media_file)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking media_file changes.

  ## Examples

      iex> change_media_file(media_file)
      %Ecto.Changeset{data: %MediaFile{}}

  """
  def change_media_file(%MediaFile{} = media_file, attrs \\ %{}) do
    MediaFile.changeset(media_file, attrs)
  end
end
