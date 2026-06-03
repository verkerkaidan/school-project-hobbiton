defmodule MediaServiceElx.MediaFilesFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.MediaFiles` context.
  """

  @doc """
  Generate a unique media_file hash.
  """
  def unique_media_file_hash do
      :crypto.strong_rand_bytes(32)
  end

  @doc """
  Generate a media_file.
  """
  def media_file_fixture(attrs \\ %{}) do
    {:ok, media_file} =
      attrs
      |> Enum.into(%{
        content_flag: MediaServiceElx.MediaFiles.MediaFile.content_flag("UNASSIGNED"),
        content_type: "image/png",
        hash: unique_media_file_hash(),
        original: "some original",
        admin_approved: false
      })
      |> MediaServiceElx.MediaFiles.create_media_file()

    media_file
  end
end
