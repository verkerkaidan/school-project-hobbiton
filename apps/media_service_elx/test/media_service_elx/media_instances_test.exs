defmodule MediaServiceElx.MediaInstancesTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.MediaInstances

  describe "media_instances" do
    alias MediaServiceElx.MediaInstances.MediaInstance

    import MediaServiceElx.MediaInstancesFixtures

    @invalid_attrs %{file_id: nil, owner_id: nil}

    test "list_media_instances/0 returns all media_instances" do
      media_instance = media_instance_fixture()
      assert MediaInstances.list_media_instances() == [media_instance]
    end

    test "get_media_instance!/1 returns the media_instance with given id" do
      media_instance = media_instance_fixture()
      assert MediaInstances.get_media_instance!(media_instance.id) == media_instance
    end

    test "create_media_instance/1 with valid data creates a media_instance" do
      valid_attrs = %{file_id: "some file_id", owner_id: "some owner_id"}

      assert {:ok, %MediaInstance{} = media_instance} = MediaInstances.create_media_instance(valid_attrs)
      assert media_instance.file_id == "some file_id"
      assert media_instance.owner_id == "some owner_id"
    end

    test "create_media_instance/1 with invalid data returns error changeset" do
      assert {:error, %Ecto.Changeset{}} = MediaInstances.create_media_instance(@invalid_attrs)
    end

    test "update_media_instance/2 with valid data updates the media_instance" do
      media_instance = media_instance_fixture()
      update_attrs = %{file_id: "some updated file_id", owner_id: "some updated owner_id"}

      assert {:ok, %MediaInstance{} = media_instance} = MediaInstances.update_media_instance(media_instance, update_attrs)
      assert media_instance.file_id == "some updated file_id"
      assert media_instance.owner_id == "some updated owner_id"
    end

    test "update_media_instance/2 with invalid data returns error changeset" do
      media_instance = media_instance_fixture()
      assert {:error, %Ecto.Changeset{}} = MediaInstances.update_media_instance(media_instance, @invalid_attrs)
      assert media_instance == MediaInstances.get_media_instance!(media_instance.id)
    end

    test "delete_media_instance/1 deletes the media_instance" do
      media_instance = media_instance_fixture()
      assert {:ok, %MediaInstance{}} = MediaInstances.delete_media_instance(media_instance)
      assert_raise Ecto.NoResultsError, fn -> MediaInstances.get_media_instance!(media_instance.id) end
    end

    test "change_media_instance/1 returns a media_instance changeset" do
      media_instance = media_instance_fixture()
      assert %Ecto.Changeset{} = MediaInstances.change_media_instance(media_instance)
    end
  end
end
