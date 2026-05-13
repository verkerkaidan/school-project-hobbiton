defmodule MediaServiceElx.MediaInstances do
  @moduledoc """
  The MediaInstances context.
  """

  import Ecto.Query, warn: false
  alias MediaServiceElx.Repo

  alias MediaServiceElx.MediaInstances.MediaInstance

  @doc """
  Returns the list of media_instances.

  ## Examples

      iex> list_media_instances()
      [%MediaInstance{}, ...]

  """
  def list_media_instances do
    Repo.all(MediaInstance)
  end

  @doc """
  Gets a single media_instance.

  Raises `Ecto.NoResultsError` if the Media instance does not exist.

  ## Examples

      iex> get_media_instance!(123)
      %MediaInstance{}

      iex> get_media_instance!(456)
      ** (Ecto.NoResultsError)

  """
  def get_media_instance!(id), do: Repo.get!(MediaInstance, id)

  @doc """
  Creates a media_instance.

  ## Examples

      iex> create_media_instance(%{field: value})
      {:ok, %MediaInstance{}}

      iex> create_media_instance(%{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def create_media_instance(attrs) do
    %MediaInstance{}
    |> MediaInstance.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a media_instance.

  ## Examples

      iex> update_media_instance(media_instance, %{field: new_value})
      {:ok, %MediaInstance{}}

      iex> update_media_instance(media_instance, %{field: bad_value})
      {:error, %Ecto.Changeset{}}

  """
  def update_media_instance(%MediaInstance{} = media_instance, attrs) do
    media_instance
    |> MediaInstance.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a media_instance.

  ## Examples

      iex> delete_media_instance(media_instance)
      {:ok, %MediaInstance{}}

      iex> delete_media_instance(media_instance)
      {:error, %Ecto.Changeset{}}

  """
  def delete_media_instance(%MediaInstance{} = media_instance) do
    Repo.delete(media_instance)
  end

  @doc """
  Returns an `%Ecto.Changeset{}` for tracking media_instance changes.

  ## Examples

      iex> change_media_instance(media_instance)
      %Ecto.Changeset{data: %MediaInstance{}}

  """
  def change_media_instance(%MediaInstance{} = media_instance, attrs \\ %{}) do
    MediaInstance.changeset(media_instance, attrs)
  end
end
