defmodule MediaServiceElx.MediaInstancesFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.MediaInstances` context.
  """

  alias MediaServiceElx.MediaInstances

  def valid_attrs do
    %{
      file_id: Ecto.UUID.generate(),
      owner_id: Ecto.UUID.generate()
    }
  end

  def insert_media_instance(attrs \\ %{}) do
    {:ok, media_instance} =
      valid_attrs()
      |> Map.merge(attrs)
      |> then(&MediaInstances.create_media_instance/1)

    media_instance
  end

end