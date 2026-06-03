defmodule MediaServiceElx.MediaFilesFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `MediaServiceElx.MediaFiles` context.
  """

  alias MediaServiceElx.MediaFiles
  alias MediaServiceElx.MediaFiles.MediaFile

  def valid_attrs do
    %{
      content_flag: MediaFile.content_flag("UNASSIGNED"),
      content_type: "image/png",
      hash: :crypto.strong_rand_bytes(32),
      original: :crypto.strong_rand_bytes(128)
    }
  end

  def insert_media_file(attrs \\ %{}) do
    {:ok, media_file} =
      valid_attrs()
      |> Map.merge(attrs)
      |> then(&MediaFiles.create_media_file/1)

    media_file
  end
end
