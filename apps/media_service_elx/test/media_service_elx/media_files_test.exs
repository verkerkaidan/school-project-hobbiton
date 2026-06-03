defmodule MediaServiceElx.MediaFilesTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.MediaFiles
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaFilesFixtures

  # ---------------------------------------------------------------------------
  # list_media_files/0
  # ---------------------------------------------------------------------------

  describe "list_media_files/0" do
    test "returns an empty list when there are no media files" do
      assert MediaFiles.list_media_files() == []
    end

    test "returns all media files" do
      media_file = MediaFilesFixtures.insert_media_file()
      ids = MediaFiles.list_media_files() |> Enum.map(& &1.id)
      assert media_file.id in ids
    end
  end

  # ---------------------------------------------------------------------------
  # get_media_file!/1
  # ---------------------------------------------------------------------------

  describe "get_media_file!/1" do
    test "returns the media file with the given id" do
      media_file = MediaFilesFixtures.insert_media_file()
      found = MediaFiles.get_media_file!(media_file.id)
      assert found.id == media_file.id
    end

    test "raises Ecto.NoResultsError for a non-existent id" do
      assert_raise Ecto.NoResultsError, fn ->
        MediaFiles.get_media_file!(Ecto.UUID.generate())
      end
    end
  end

  # ---------------------------------------------------------------------------
  # create_media_file/1
  # ---------------------------------------------------------------------------

  describe "create_media_file/1" do
    test "creates a media file with valid attributes" do
      assert {:ok, %MediaFile{} = media_file} = MediaFiles.create_media_file(MediaFilesFixtures.valid_attrs())
      assert media_file.content_type == "image/png"
      assert media_file.content_flag == MediaFile.content_flag("UNASSIGNED")
    end

    test "returns error changeset when required fields are missing" do
      assert {:error, %Ecto.Changeset{}} = MediaFiles.create_media_file(%{})
    end

    test "returns error changeset when content_flag is missing" do
      attrs = MediaFilesFixtures.valid_attrs() |> Map.delete(:content_flag)
      assert {:error, changeset} = MediaFiles.create_media_file(attrs)
      assert %{content_flag: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when content_type is missing" do
      attrs = MediaFilesFixtures.valid_attrs() |> Map.delete(:content_type)
      assert {:error, changeset} = MediaFiles.create_media_file(attrs)
      assert %{content_type: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when hash is missing" do
      attrs = MediaFilesFixtures.valid_attrs() |> Map.delete(:hash)
      assert {:error, changeset} = MediaFiles.create_media_file(attrs)
      assert %{hash: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when original is missing" do
      attrs = MediaFilesFixtures.valid_attrs() |> Map.delete(:original)
      assert {:error, changeset} = MediaFiles.create_media_file(attrs)
      assert %{original: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when content_flag is not a valid value" do
      attrs = %{MediaFilesFixtures.valid_attrs() | content_flag: 99}
      assert {:error, changeset} = MediaFiles.create_media_file(attrs)
      assert %{content_flag: ["is invalid"]} = errors_on(changeset)
    end

    test "returns error changeset on duplicate hash" do
      attrs = MediaFilesFixtures.valid_attrs()
      assert {:ok, _} = MediaFiles.create_media_file(attrs)
      assert {:error, changeset} = MediaFiles.create_media_file(attrs)
      assert %{hash: ["has already been taken"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # update_media_file/2
  # ---------------------------------------------------------------------------

  describe "update_media_file/2" do
    test "updates a media file with valid attributes" do
      media_file = MediaFilesFixtures.insert_media_file()
      new_content_type = "video/mp4"

      assert {:ok, updated} =
               MediaFiles.update_media_file(media_file, %{content_type: new_content_type})

      assert updated.content_type == new_content_type
    end

    test "updates admin_approved flag" do
      media_file = MediaFilesFixtures.insert_media_file()
      assert {:ok, updated} = MediaFiles.update_media_file(media_file, %{admin_approved: true})
      assert updated.admin_approved == true
    end

    test "returns error changeset for invalid content_flag" do
      media_file = MediaFilesFixtures.insert_media_file()
      assert {:error, changeset} = MediaFiles.update_media_file(media_file, %{content_flag: 99})
      assert %{content_flag: ["is invalid"]} = errors_on(changeset)
    end

    test "returns error changeset when required field is set to nil" do
      media_file = MediaFilesFixtures.insert_media_file()
      assert {:error, changeset} = MediaFiles.update_media_file(media_file, %{content_type: nil})
      assert %{content_type: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_media_file/1
  # ---------------------------------------------------------------------------

  describe "delete_media_file/1" do
    test "deletes a media file" do
      media_file = MediaFilesFixtures.insert_media_file()
      assert {:ok, %MediaFile{}} = MediaFiles.delete_media_file(media_file)

      assert_raise Ecto.NoResultsError, fn ->
        MediaFiles.get_media_file!(media_file.id)
      end
    end
  end

  # ---------------------------------------------------------------------------
  # change_media_file/2
  # ---------------------------------------------------------------------------

  describe "change_media_file/2" do
    test "returns a changeset" do
      media_file = MediaFilesFixtures.insert_media_file()
      assert %Ecto.Changeset{} = MediaFiles.change_media_file(media_file)
    end

    test "returns a changeset reflecting given attrs" do
      media_file = MediaFilesFixtures.insert_media_file()
      changeset = MediaFiles.change_media_file(media_file, %{content_type: "audio/mp3"})
      assert changeset.changes.content_type == "audio/mp3"
    end
  end

  # ---------------------------------------------------------------------------
  # MediaFile.changeset/2 — content_flag values
  # ---------------------------------------------------------------------------

  describe "MediaFile.content_flag/1" do
    test "SAFE maps to 0" do
      assert MediaFile.content_flag("SAFE") == 0
    end

    test "RISK maps to 1" do
      assert MediaFile.content_flag("RISK") == 1
    end

    test "UNASSIGNED maps to 2" do
      assert MediaFile.content_flag("UNASSIGNED") == 2
    end

    test "all valid content_flag values are accepted by changeset" do
      for {_key, value} <- %{"SAFE" => 0, "RISK" => 1, "UNASSIGNED" => 2} do
        attrs = %{MediaFilesFixtures.valid_attrs() | content_flag: value, hash: :crypto.strong_rand_bytes(32)}
        assert {:ok, _} = MediaFiles.create_media_file(attrs)
      end
    end
  end
end