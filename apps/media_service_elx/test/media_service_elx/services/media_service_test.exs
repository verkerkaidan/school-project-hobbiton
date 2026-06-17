defmodule MediaServiceElx.MediaServiceTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.FileReports
  alias MediaServiceElx.FileReportsFixtures
  alias MediaServiceElx.MediaFiles
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaFilesFixtures
  alias MediaServiceElx.MediaInstances
  alias MediaServiceElx.MediaInstancesFixtures
  alias MediaServiceElx.MediaService

  # ---------------------------------------------------------------------------
  # Helpers
  # ---------------------------------------------------------------------------

  defp upload_fixture do
    %Plug.Upload{
      path: Path.expand("../../support/files/test.png", __DIR__),
      filename: "test.png",
      content_type: "image/png"
    }
  end

  defp upload_hash do
    upload_fixture().path
    |> File.stream!([], 2048)
    |> Enum.reduce(:crypto.hash_init(:sha256), &:crypto.hash_update(&2, &1))
    |> :crypto.hash_final()
  end

  # ---------------------------------------------------------------------------
  # upload_media_file/2
  # ---------------------------------------------------------------------------

  describe "upload_media_file/2" do
    test "creates a media file and media instance for a new upload" do
      owner_id = Ecto.UUID.generate()

      assert {:ok, instance_id} = MediaService.upload_media_file(upload_fixture(), owner_id)

      media_instance = MediaInstances.get_media_instance!(instance_id)
      media_file = MediaFiles.get_media_file!(media_instance.file_id)

      assert media_instance.owner_id == owner_id
      assert media_file.content_type == "image/png"
      assert media_file.content_flag == MediaFile.content_flag("UNASSIGNED")
      assert media_file.hash == upload_hash()
      assert media_file.original == File.read!(upload_fixture().path)
    end

    test "reuses an existing media file and creates a new instance for the same upload" do
      owner_id_1 = Ecto.UUID.generate()
      owner_id_2 = Ecto.UUID.generate()

      assert {:ok, instance_id_1} = MediaService.upload_media_file(upload_fixture(), owner_id_1)
      assert {:ok, instance_id_2} = MediaService.upload_media_file(upload_fixture(), owner_id_2)

      instance_1 = MediaInstances.get_media_instance!(instance_id_1)
      instance_2 = MediaInstances.get_media_instance!(instance_id_2)

      assert instance_1.file_id == instance_2.file_id
      assert instance_1.owner_id == owner_id_1
      assert instance_2.owner_id == owner_id_2
      refute instance_2.id == instance_1.id
      assert length(MediaFiles.list_media_files()) == 1
      assert length(MediaInstances.list_media_instances()) == 2
    end

    test "returns banned error when the uploaded file hash is reported" do
      FileReportsFixtures.insert_reported_file(%{hash: upload_hash()})

      assert {:error, :banned} =
               MediaService.upload_media_file(upload_fixture(), Ecto.UUID.generate())

      assert MediaFiles.list_media_files() == []
      assert MediaInstances.list_media_instances() == []
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_unassigned/0
  # ---------------------------------------------------------------------------

  describe "retrieve_unassigned/0" do
    test "returns only media files with the unassigned content flag" do
      unassigned =
        MediaFilesFixtures.insert_media_file(%{
          content_flag: MediaFile.content_flag("UNASSIGNED")
        })

      MediaFilesFixtures.insert_media_file(%{content_flag: MediaFile.content_flag("SAFE")})

      ids = MediaService.retrieve_unassigned() |> Enum.map(& &1.id)

      assert ids == [unassigned.id]
    end

    test "returns an empty list when no unassigned media files exist" do
      MediaFilesFixtures.insert_media_file(%{content_flag: MediaFile.content_flag("SAFE")})

      assert MediaService.retrieve_unassigned() == []
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_instances_by_owner/1
  # ---------------------------------------------------------------------------

  describe "retrieve_instances_by_owner/1" do
    test "returns all media instances for the given owner" do
      owner_id = Ecto.UUID.generate()
      media_instance = MediaInstancesFixtures.insert_media_instance(%{owner_id: owner_id})
      MediaInstancesFixtures.insert_media_instance()

      ids = MediaService.retrieve_instances_by_owner(owner_id) |> Enum.map(& &1.id)

      assert ids == [media_instance.id]
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_media_file/1
  # ---------------------------------------------------------------------------

  describe "retrieve_media_file/1" do
    test "returns a media file for an existing media instance" do
      media_file = MediaFilesFixtures.insert_media_file()
      media_instance = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})

      assert {:ok, found} = MediaService.retrieve_media_file(media_instance.id)
      assert found.id == media_file.id
    end

    test "returns an error when the media instance does not exist" do
      assert {:error, "MediaInstance not found"} =
               MediaService.retrieve_media_file(Ecto.UUID.generate())
    end

    test "returns an error when the media file does not exist" do
      media_instance =
        MediaInstancesFixtures.insert_media_instance(%{file_id: Ecto.UUID.generate()})

      assert {:error, "MediaFile not found"} = MediaService.retrieve_media_file(media_instance.id)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_file/1
  # ---------------------------------------------------------------------------

  describe "delete_file/1" do
    test "deletes the media file and all linked media instances" do
      media_file = MediaFilesFixtures.insert_media_file()
      media_instance_1 = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})
      media_instance_2 = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})

      assert {:ok, file_id} = MediaService.delete_file(media_file)
      assert file_id == media_file.id

      assert_raise Ecto.NoResultsError, fn -> MediaFiles.get_media_file!(media_file.id) end

      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(media_instance_1.id)
      end

      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(media_instance_2.id)
      end
    end
  end

  # ---------------------------------------------------------------------------
  # delete_single_instance/1
  # ---------------------------------------------------------------------------

  describe "delete_single_instance/1" do
    test "deletes only the instance when other instances still reference the file" do
      media_file = MediaFilesFixtures.insert_media_file()
      media_instance_1 = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})
      media_instance_2 = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})

      assert {:ok, instance_id} = MediaService.delete_single_instance(media_instance_1.id)
      assert instance_id == media_instance_1.id

      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(media_instance_1.id)
      end

      assert MediaInstances.get_media_instance!(media_instance_2.id)
      assert MediaFiles.get_media_file!(media_file.id)
    end

    test "deletes the media file when the deleted instance was the last reference" do
      media_file = MediaFilesFixtures.insert_media_file()
      media_instance = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})

      assert {:ok, file_id} = MediaService.delete_single_instance(media_instance.id)
      assert file_id == media_file.id

      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(media_instance.id)
      end

      assert_raise Ecto.NoResultsError, fn -> MediaFiles.get_media_file!(media_file.id) end
    end

    test "returns an error when the media instance does not exist" do
      assert {:error, "MediaInstance not found"} =
               MediaService.delete_single_instance(Ecto.UUID.generate())
    end
  end

  # ---------------------------------------------------------------------------
  # delete_all_instances/1
  # ---------------------------------------------------------------------------

  describe "delete_all_instances/1" do
    test "returns ok status for each successfully deleted instance" do
      media_file = MediaFilesFixtures.insert_media_file()
      media_instance_1 = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})
      media_instance_2 = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})

      assert [
               %{instance_id: instance_id_1, status: "ok"},
               %{instance_id: instance_id_2, status: "ok"}
             ] = MediaService.delete_all_instances([media_instance_1, media_instance_2])

      assert instance_id_1 == media_instance_1.id
      assert instance_id_2 == media_file.id
    end
  end

  # ---------------------------------------------------------------------------
  # banned?/1
  # ---------------------------------------------------------------------------

  describe "banned?/1" do
    test "returns true when a reported file exists for the hash" do
      reported_file = FileReportsFixtures.insert_reported_file()

      assert MediaService.banned?(reported_file.hash)
    end

    test "returns false when no reported file exists for the hash" do
      refute MediaService.banned?(:crypto.strong_rand_bytes(32))
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_media_files_from_file_id/1
  # ---------------------------------------------------------------------------

  describe "retrieve_media_files_from_file_id/1" do
    test "returns existing media files and ignores missing ids" do
      media_file = MediaFilesFixtures.insert_media_file()

      assert %{files: [found]} =
               MediaService.retrieve_media_files_from_file_id([
                 media_file.id,
                 Ecto.UUID.generate()
               ])

      assert found.id == media_file.id
    end
  end

  # ---------------------------------------------------------------------------
  # update_content_flag/3
  # ---------------------------------------------------------------------------

  describe "update_content_flag/3" do
    test "updates the content flag for a matching base64 encoded hash" do
      media_file = MediaFilesFixtures.insert_media_file()
      encoded_hash = Base.encode64(media_file.hash)

      assert {:ok, file_id} =
               MediaService.update_content_flag(
                 MediaFile.content_flag("SAFE"),
                 encoded_hash,
                 "AI driven change"
               )

      assert file_id == media_file.id

      assert MediaFiles.get_media_file!(media_file.id).content_flag ==
               MediaFile.content_flag("SAFE")
    end

    test "removes reports for the updated media file" do
      media_file = MediaFilesFixtures.insert_media_file()
      file_report = FileReportsFixtures.insert_file_report(%{file_id: media_file.id})

      assert {:ok, _file_id} =
               MediaService.update_content_flag(
                 MediaFile.content_flag("RISK"),
                 Base.encode64(media_file.hash),
                 "AI driven change"
               )

      assert_raise Ecto.NoResultsError, fn -> FileReports.get_file_report!(file_report.id) end
    end

    test "does not overwrite an admin-approved media file for an AI driven change" do
      media_file =
        MediaFilesFixtures.insert_media_file(%{
          admin_approved: true,
          content_flag: MediaFile.content_flag("SAFE")
        })

      assert {:ok, file_id} =
               MediaService.update_content_flag(
                 MediaFile.content_flag("RISK"),
                 Base.encode64(media_file.hash),
                 "AI driven change"
               )

      assert file_id == media_file.id

      assert MediaFiles.get_media_file!(media_file.id).content_flag ==
               MediaFile.content_flag("SAFE")
    end

    test "returns not found when no media file exists for the hash" do
      encoded_hash = Base.encode64(:crypto.strong_rand_bytes(32))

      assert {:error, :not_found} =
               MediaService.update_content_flag(
                 MediaFile.content_flag("SAFE"),
                 encoded_hash,
                 "AI driven change"
               )
    end
  end
end
