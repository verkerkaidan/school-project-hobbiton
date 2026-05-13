defmodule MediaServiceElx.MediaInstancesFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.MediaInstances` context.
  """

  @doc """
  Generate a media_instance.
  """
  def media_instance_fixture(attrs \\ %{}) do
    {:ok, media_instance} =
      attrs
      |> Enum.into(%{
        file_id: "some file_id",
        owner_id: "some owner_id"
      })
      |> MediaServiceElx.MediaInstances.create_media_instance()

    media_instance
  end
end
