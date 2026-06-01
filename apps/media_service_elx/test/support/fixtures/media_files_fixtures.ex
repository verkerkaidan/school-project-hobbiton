defmodule MediaServiceElx.MediaFilesFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.MediaFiles` context.
  """

  @doc """
  Generate a unique media_file hash.
  """
  def unique_media_file_hash do
    raise "implement the logic to generate a unique media_file hash"
  end

  @doc """
  Generate a media_file.
  """
  def media_file_fixture(attrs \\ %{}) do
    {:ok, media_file} =
      attrs
      |> Enum.into(%{
        content_flag: 42,
        content_type: "some content_type",
        hash: unique_media_file_hash(),
        original: "some original",
        thumbnail: "some thumbnail"
      })
      |> MediaServiceElx.MediaFiles.create_media_file()

    media_file
  end
end
