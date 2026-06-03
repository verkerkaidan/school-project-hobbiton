defmodule MediaServiceElx.MediaInstancesTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.MediaInstances.MediaInstance
  alias MediaServiceElx.MediaInstances
  alias MediaServiceElx.MediaInstancesFixtures

  # ---------------------------------------------------------------------------
  # list_media_instances/0
  # ---------------------------------------------------------------------------

  describe "list_media_instances/0" do
    test "returns an empty list when there are no media instances" do
      assert MediaInstances.list_media_instances() == []
    end

    test "returns all media instances" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      ids = MediaInstances.list_media_instances() |> Enum.map(& &1.id)
      assert media_instance.id in ids
    end

    test "returns multiple media instances" do
      MediaInstancesFixtures.insert_media_instance()
      MediaInstancesFixtures.insert_media_instance()
      assert length(MediaInstances.list_media_instances()) == 2
    end
  end

  # ---------------------------------------------------------------------------
  # get_media_instance!/1
  # ---------------------------------------------------------------------------

  describe "get_media_instance!/1" do
    test "returns the media instance with the given id" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      found = MediaInstances.get_media_instance!(media_instance.id)
      assert found.id == media_instance.id
    end

    test "returns the correct file_id and owner_id" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      found = MediaInstances.get_media_instance!(media_instance.id)
      assert found.file_id == media_instance.file_id
      assert found.owner_id == media_instance.owner_id
    end

    test "raises Ecto.NoResultsError for a non-existent id" do
      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(Ecto.UUID.generate())
      end
    end
  end

  # ---------------------------------------------------------------------------
  # create_media_instance/1
  # ---------------------------------------------------------------------------

  describe "create_media_instance/1" do
    test "creates a media instance with valid attributes" do
      assert {:ok, %MediaInstance{} = media_instance} =
               MediaInstances.create_media_instance(MediaInstancesFixtures.valid_attrs())

      assert media_instance.file_id != nil
      assert media_instance.owner_id != nil
    end

    test "returns error changeset when all fields are missing" do
      assert {:error, %Ecto.Changeset{}} = MediaInstances.create_media_instance(%{})
    end

    test "returns error changeset when file_id is missing" do
      attrs = MediaInstancesFixtures.valid_attrs() |> Map.delete(:file_id)
      assert {:error, changeset} = MediaInstances.create_media_instance(attrs)
      assert %{file_id: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when owner_id is missing" do
      attrs = MediaInstancesFixtures.valid_attrs() |> Map.delete(:owner_id)
      assert {:error, changeset} = MediaInstances.create_media_instance(attrs)
      assert %{owner_id: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # update_media_instance/2
  # ---------------------------------------------------------------------------

  describe "update_media_instance/2" do
    test "updates a media instance with valid attributes" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      new_owner_id = Ecto.UUID.generate()

      assert {:ok, updated} =
               MediaInstances.update_media_instance(media_instance, %{owner_id: new_owner_id})

      assert updated.owner_id == new_owner_id
    end

    test "updates file_id with a new value" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      new_file_id = Ecto.UUID.generate()

      assert {:ok, updated} =
               MediaInstances.update_media_instance(media_instance, %{file_id: new_file_id})

      assert updated.file_id == new_file_id
    end

    test "returns error changeset when file_id is set to nil" do
      media_instance = MediaInstancesFixtures.insert_media_instance()

      assert {:error, changeset} =
               MediaInstances.update_media_instance(media_instance, %{file_id: nil})

      assert %{file_id: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when owner_id is set to nil" do
      media_instance = MediaInstancesFixtures.insert_media_instance()

      assert {:error, changeset} =
               MediaInstances.update_media_instance(media_instance, %{owner_id: nil})

      assert %{owner_id: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_media_instance/1
  # ---------------------------------------------------------------------------

  describe "delete_media_instance/1" do
    test "deletes a media instance" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      assert {:ok, %MediaInstance{}} = MediaInstances.delete_media_instance(media_instance)

      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(media_instance.id)
      end
    end

    test "does not affect other media instances when one is deleted" do
      media_instance_1 = MediaInstancesFixtures.insert_media_instance()
      media_instance_2 = MediaInstancesFixtures.insert_media_instance()

      assert {:ok, _} = MediaInstances.delete_media_instance(media_instance_1)

      assert MediaInstances.get_media_instance!(media_instance_2.id)
    end
  end

  # ---------------------------------------------------------------------------
  # change_media_instance/2
  # ---------------------------------------------------------------------------

  describe "change_media_instance/2" do
    test "returns a changeset" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      assert %Ecto.Changeset{} = MediaInstances.change_media_instance(media_instance)
    end

    test "returns a changeset reflecting given attrs" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      new_owner_id = Ecto.UUID.generate()

      changeset =
        MediaInstances.change_media_instance(media_instance, %{owner_id: new_owner_id})

      assert changeset.changes.owner_id == new_owner_id
    end

    test "defaults to empty attrs" do
      media_instance = MediaInstancesFixtures.insert_media_instance()
      changeset = MediaInstances.change_media_instance(media_instance)
      assert changeset.changes == %{}
    end
  end
end