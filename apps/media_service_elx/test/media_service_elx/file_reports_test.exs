defmodule MediaServiceElx.FileReportsTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.FileReports
  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.FileReports.ReportedFile
  alias MediaServiceElx.FileReportsFixtures

  # ===========================================================================
  # FileReport
  # ===========================================================================

  # ---------------------------------------------------------------------------
  # list_file_report/0
  # ---------------------------------------------------------------------------

  describe "list_file_report/0" do
    test "returns an empty list when there are no file reports" do
      assert FileReports.list_file_report() == []
    end

    test "returns all file reports" do
      file_report = FileReportsFixtures.insert_file_report()
      ids = FileReports.list_file_report() |> Enum.map(& &1.id)
      assert file_report.id in ids
    end

    test "returns multiple file reports" do
      FileReportsFixtures.insert_file_report()
      FileReportsFixtures.insert_file_report()
      assert length(FileReports.list_file_report()) == 2
    end
  end

  # ---------------------------------------------------------------------------
  # get_file_report!/1
  # ---------------------------------------------------------------------------

  describe "get_file_report!/1" do
    test "returns the file report with the given id" do
      file_report = FileReportsFixtures.insert_file_report()
      found = FileReports.get_file_report!(file_report.id)
      assert found.id == file_report.id
    end

    test "returns the correct file_id and user_id" do
      file_report = FileReportsFixtures.insert_file_report()
      found = FileReports.get_file_report!(file_report.id)
      assert found.file_id == file_report.file_id
      assert found.user_id == file_report.user_id
    end

    test "raises Ecto.NoResultsError for a non-existent id" do
      assert_raise Ecto.NoResultsError, fn ->
        FileReports.get_file_report!(Ecto.UUID.generate())
      end
    end
  end

  # ---------------------------------------------------------------------------
  # create_file_report/1
  # ---------------------------------------------------------------------------

  describe "create_file_report/1" do
    test "creates a file report with valid attributes" do
      assert {:ok, %FileReport{} = file_report} =
               FileReports.create_file_report(FileReportsFixtures.valid_file_report_attrs())

      assert file_report.file_id != nil
      assert file_report.user_id != nil
    end

    test "returns error changeset when all fields are missing" do
      assert {:error, %Ecto.Changeset{}} = FileReports.create_file_report(%{})
    end

    test "returns error changeset when file_id is missing" do
      attrs = FileReportsFixtures.valid_file_report_attrs() |> Map.delete(:file_id)
      assert {:error, changeset} = FileReports.create_file_report(attrs)
      assert %{file_id: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when user_id is missing" do
      attrs = FileReportsFixtures.valid_file_report_attrs() |> Map.delete(:user_id)
      assert {:error, changeset} = FileReports.create_file_report(attrs)
      assert %{user_id: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # update_file_report/2
  # ---------------------------------------------------------------------------

  describe "update_file_report/2" do
    test "updates a file report with a new file_id" do
      file_report = FileReportsFixtures.insert_file_report()
      new_file_id = Ecto.UUID.generate()

      assert {:ok, updated} =
               FileReports.update_file_report(file_report, %{file_id: new_file_id})

      assert updated.file_id == new_file_id
    end

    test "updates a file report with a new user_id" do
      file_report = FileReportsFixtures.insert_file_report()
      new_user_id = Ecto.UUID.generate()

      assert {:ok, updated} =
               FileReports.update_file_report(file_report, %{user_id: new_user_id})

      assert updated.user_id == new_user_id
    end

    test "returns error changeset when file_id is set to nil" do
      file_report = FileReportsFixtures.insert_file_report()

      assert {:error, changeset} =
               FileReports.update_file_report(file_report, %{file_id: nil})

      assert %{file_id: ["can't be blank"]} = errors_on(changeset)
    end

    test "returns error changeset when user_id is set to nil" do
      file_report = FileReportsFixtures.insert_file_report()

      assert {:error, changeset} =
               FileReports.update_file_report(file_report, %{user_id: nil})

      assert %{user_id: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_file_report/1
  # ---------------------------------------------------------------------------

  describe "delete_file_report/1" do
    test "deletes a file report" do
      file_report = FileReportsFixtures.insert_file_report()
      assert {:ok, %FileReport{}} = FileReports.delete_file_report(file_report)

      assert_raise Ecto.NoResultsError, fn ->
        FileReports.get_file_report!(file_report.id)
      end
    end

    test "does not affect other file reports when one is deleted" do
      file_report_1 = FileReportsFixtures.insert_file_report()
      file_report_2 = FileReportsFixtures.insert_file_report()

      assert {:ok, _} = FileReports.delete_file_report(file_report_1)
      assert FileReports.get_file_report!(file_report_2.id)
    end
  end

  # ---------------------------------------------------------------------------
  # change_file_report/2
  # ---------------------------------------------------------------------------

  describe "change_file_report/2" do
    test "returns a changeset" do
      file_report = FileReportsFixtures.insert_file_report()
      assert %Ecto.Changeset{} = FileReports.change_file_report(file_report)
    end

    test "returns a changeset reflecting given attrs" do
      file_report = FileReportsFixtures.insert_file_report()
      new_user_id = Ecto.UUID.generate()

      changeset = FileReports.change_file_report(file_report, %{user_id: new_user_id})
      assert changeset.changes.user_id == new_user_id
    end

    test "defaults to empty attrs" do
      file_report = FileReportsFixtures.insert_file_report()
      changeset = FileReports.change_file_report(file_report)
      assert changeset.changes == %{}
    end
  end

  # ===========================================================================
  # ReportedFile
  # ===========================================================================

  # ---------------------------------------------------------------------------
  # list_reported_file/0
  # ---------------------------------------------------------------------------

  describe "list_reported_file/0" do
    test "returns an empty list when there are no reported files" do
      assert FileReports.list_reported_file() == []
    end

    test "returns all reported files" do
      reported_file = FileReportsFixtures.insert_reported_file()
      ids = FileReports.list_reported_file() |> Enum.map(& &1.id)
      assert reported_file.id in ids
    end

    test "returns multiple reported files" do
      FileReportsFixtures.insert_reported_file()
      FileReportsFixtures.insert_reported_file()
      assert length(FileReports.list_reported_file()) == 2
    end
  end

  # ---------------------------------------------------------------------------
  # get_reported_file!/1
  # ---------------------------------------------------------------------------

  describe "get_reported_file!/1" do
    test "returns the reported file with the given id" do
      reported_file = FileReportsFixtures.insert_reported_file()
      found = FileReports.get_reported_file!(reported_file.id)
      assert found.id == reported_file.id
    end

    test "raises Ecto.NoResultsError for a non-existent id" do
      assert_raise Ecto.NoResultsError, fn ->
        FileReports.get_reported_file!(Ecto.UUID.generate())
      end
    end
  end

  # ---------------------------------------------------------------------------
  # create_reported_file/1
  # ---------------------------------------------------------------------------

  describe "create_reported_file/1" do
    test "creates a reported file with valid attributes" do
      assert {:ok, %ReportedFile{} = reported_file} =
               FileReports.create_reported_file(FileReportsFixtures.valid_reported_file_attrs())

      assert reported_file.hash != nil
    end

    test "returns error changeset when hash is missing" do
      assert {:error, changeset} = FileReports.create_reported_file(%{})
      assert %{hash: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # update_reported_file/2
  # ---------------------------------------------------------------------------

  describe "update_reported_file/2" do
    test "updates a reported file with a new hash" do
      reported_file = FileReportsFixtures.insert_reported_file()
      new_hash = :crypto.strong_rand_bytes(32)

      assert {:ok, updated} =
               FileReports.update_reported_file(reported_file, %{hash: new_hash})

      assert updated.hash == new_hash
    end

    test "returns error changeset when hash is set to nil" do
      reported_file = FileReportsFixtures.insert_reported_file()

      assert {:error, changeset} =
               FileReports.update_reported_file(reported_file, %{hash: nil})

      assert %{hash: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_reported_file/1
  # ---------------------------------------------------------------------------

  describe "delete_reported_file/1" do
    test "deletes a reported file" do
      reported_file = FileReportsFixtures.insert_reported_file()
      assert {:ok, %ReportedFile{}} = FileReports.delete_reported_file(reported_file)

      assert_raise Ecto.NoResultsError, fn ->
        FileReports.get_reported_file!(reported_file.id)
      end
    end

    test "does not affect other reported files when one is deleted" do
      reported_file_1 = FileReportsFixtures.insert_reported_file()
      reported_file_2 = FileReportsFixtures.insert_reported_file()

      assert {:ok, _} = FileReports.delete_reported_file(reported_file_1)
      assert FileReports.get_reported_file!(reported_file_2.id)
    end
  end

  # ---------------------------------------------------------------------------
  # change_reported_file/2
  # ---------------------------------------------------------------------------

  describe "change_reported_file/2" do
    test "returns a changeset" do
      reported_file = FileReportsFixtures.insert_reported_file()
      assert %Ecto.Changeset{} = FileReports.change_reported_file(reported_file)
    end

    test "returns a changeset reflecting given attrs" do
      reported_file = FileReportsFixtures.insert_reported_file()
      new_hash = :crypto.strong_rand_bytes(32)

      changeset = FileReports.change_reported_file(reported_file, %{hash: new_hash})
      assert changeset.changes.hash == new_hash
    end

    test "defaults to empty attrs" do
      reported_file = FileReportsFixtures.insert_reported_file()
      changeset = FileReports.change_reported_file(reported_file)
      assert changeset.changes == %{}
    end
  end
end
