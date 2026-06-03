defmodule MediaServiceElx.MediaFilesTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.MediaFiles

  describe "media_files" do
    alias MediaServiceElx.MediaFiles.MediaFile

    import MediaServiceElx.MediaFilesFixtures

    @invalid_attrs %{original: nil, hash: nil, content_flag: nil, content_type: nil}

    test "list_media_files/0 returns all media_files" do
      media_file = media_file_fixture()
      assert MediaFiles.list_media_files() == [media_file]
    end

    test "get_media_file!/1 returns the media_file with given id" do
      media_file = media_file_fixture()
      assert MediaFiles.get_media_file!(media_file.id) == media_file
    end

    test "create_media_file/1 with valid data creates a media_file" do
      valid_attrs = %{original: "some original", hash: "some hash", content_flag: MediaServiceElx.MediaFiles.MediaFile.content_flag("UNASSIGNED"), content_type: "some content_type"}

      assert {:ok, %MediaFile{} = media_file} = MediaFiles.create_media_file(valid_attrs)
      assert media_file.original == "some original"
      assert media_file.hash == "some hash"
      assert media_file.content_flag == MediaServiceElx.MediaFiles.MediaFile.content_flag("UNASSIGNED")
      assert media_file.content_type == "some content_type"
    end

    test "create_media_file/1 with invalid data returns error changeset" do
      assert {:error, %Ecto.Changeset{}} = MediaFiles.create_media_file(@invalid_attrs)
    end

    test "update_media_file/2 with valid data updates the media_file" do
      media_file = media_file_fixture()
      update_attrs = %{original: "some updated original", hash: "some updated hash", content_flag: MediaServiceElx.MediaFiles.MediaFile.content_flag("UNASSIGNED"), content_type: "some updated content_type"}

      assert {:ok, %MediaFile{} = media_file} = MediaFiles.update_media_file(media_file, update_attrs)
      assert media_file.original == "some updated original"
      assert media_file.hash == "some updated hash"
      assert media_file.content_flag == MediaServiceElx.MediaFiles.MediaFile.content_flag("UNASSIGNED")
      assert media_file.content_type == "some updated content_type"
    end

    test "update_media_file/2 with invalid data returns error changeset" do
      media_file = media_file_fixture()

      assert {:error, %Ecto.Changeset{}} = MediaFiles.update_media_file(media_file, @invalid_attrs)
      assert media_file == MediaFiles.get_media_file!(media_file.id)
    end

    test "delete_media_file/1 deletes the media_file" do
      media_file = media_file_fixture()
      assert {:ok, %MediaFile{}} = MediaFiles.delete_media_file(media_file)
      assert_raise Ecto.NoResultsError, fn -> MediaFiles.get_media_file!(media_file.id) end
    end

    test "change_media_file/1 returns a media_file changeset" do
      media_file = media_file_fixture()
      assert %Ecto.Changeset{} = MediaFiles.change_media_file(media_file)
    end
  end
end
