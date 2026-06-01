defmodule MediaServiceElx.FileReportsTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.FileReports

  describe "file_report" do
    alias MediaServiceElx.FileReports.FileReport

    import MediaServiceElx.FileReportsFixtures

    @invalid_attrs %{user_id: nil, file_id: nil}

    test "list_file_report/0 returns all file_report" do
      file_report = file_report_fixture()
      assert FileReports.list_file_report() == [file_report]
    end

    test "get_file_report!/1 returns the file_report with given id" do
      file_report = file_report_fixture()
      assert FileReports.get_file_report!(file_report.id) == file_report
    end

    test "create_file_report/1 with valid data creates a file_report" do
      valid_attrs = %{user_id: "some user_id", file_id: "some file_id"}

      assert {:ok, %FileReport{} = file_report} = FileReports.create_file_report(valid_attrs)
      assert file_report.user_id == "some user_id"
      assert file_report.file_id == "some file_id"
    end

    test "create_file_report/1 with invalid data returns error changeset" do
      assert {:error, %Ecto.Changeset{}} = FileReports.create_file_report(@invalid_attrs)
    end

    test "update_file_report/2 with valid data updates the file_report" do
      file_report = file_report_fixture()
      update_attrs = %{user_id: "some updated user_id", file_id: "some updated file_id"}

      assert {:ok, %FileReport{} = file_report} = FileReports.update_file_report(file_report, update_attrs)
      assert file_report.user_id == "some updated user_id"
      assert file_report.file_id == "some updated file_id"
    end

    test "update_file_report/2 with invalid data returns error changeset" do
      file_report = file_report_fixture()
      assert {:error, %Ecto.Changeset{}} = FileReports.update_file_report(file_report, @invalid_attrs)
      assert file_report == FileReports.get_file_report!(file_report.id)
    end

    test "delete_file_report/1 deletes the file_report" do
      file_report = file_report_fixture()
      assert {:ok, %FileReport{}} = FileReports.delete_file_report(file_report)
      assert_raise Ecto.NoResultsError, fn -> FileReports.get_file_report!(file_report.id) end
    end

    test "change_file_report/1 returns a file_report changeset" do
      file_report = file_report_fixture()
      assert %Ecto.Changeset{} = FileReports.change_file_report(file_report)
    end
  end

  describe "reported_file" do
    alias MediaServiceElx.FileReports.ReportedFile

    import MediaServiceElx.FileReportsFixtures

    @invalid_attrs %{hash: nil}

    test "list_reported_file/0 returns all reported_file" do
      reported_file = reported_file_fixture()
      assert FileReports.list_reported_file() == [reported_file]
    end

    test "get_reported_file!/1 returns the reported_file with given id" do
      reported_file = reported_file_fixture()
      assert FileReports.get_reported_file!(reported_file.id) == reported_file
    end

    test "create_reported_file/1 with valid data creates a reported_file" do
      valid_attrs = %{hash: "some hash"}

      assert {:ok, %ReportedFile{} = reported_file} = FileReports.create_reported_file(valid_attrs)
      assert reported_file.hash == "some hash"
    end

    test "create_reported_file/1 with invalid data returns error changeset" do
      assert {:error, %Ecto.Changeset{}} = FileReports.create_reported_file(@invalid_attrs)
    end

    test "update_reported_file/2 with valid data updates the reported_file" do
      reported_file = reported_file_fixture()
      update_attrs = %{hash: "some updated hash"}

      assert {:ok, %ReportedFile{} = reported_file} = FileReports.update_reported_file(reported_file, update_attrs)
      assert reported_file.hash == "some updated hash"
    end

    test "update_reported_file/2 with invalid data returns error changeset" do
      reported_file = reported_file_fixture()
      assert {:error, %Ecto.Changeset{}} = FileReports.update_reported_file(reported_file, @invalid_attrs)
      assert reported_file == FileReports.get_reported_file!(reported_file.id)
    end

    test "delete_reported_file/1 deletes the reported_file" do
      reported_file = reported_file_fixture()
      assert {:ok, %ReportedFile{}} = FileReports.delete_reported_file(reported_file)
      assert_raise Ecto.NoResultsError, fn -> FileReports.get_reported_file!(reported_file.id) end
    end

    test "change_reported_file/1 returns a reported_file changeset" do
      reported_file = reported_file_fixture()
      assert %Ecto.Changeset{} = FileReports.change_reported_file(reported_file)
    end
  end
end
