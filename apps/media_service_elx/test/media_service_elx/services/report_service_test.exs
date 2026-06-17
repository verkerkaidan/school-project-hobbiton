defmodule MediaServiceElx.ReportServiceTest do
  use MediaServiceElx.DataCase

  alias MediaServiceElx.FileReports
  alias MediaServiceElx.FileReports.FileReport
  alias MediaServiceElx.FileReports.ReportedFile
  alias MediaServiceElx.FileReportsFixtures
  alias MediaServiceElx.MediaFiles
  alias MediaServiceElx.MediaFilesFixtures
  alias MediaServiceElx.MediaInstances
  alias MediaServiceElx.MediaInstancesFixtures
  alias MediaServiceElx.ReportService

  # ---------------------------------------------------------------------------
  # create_file_report/2
  # ---------------------------------------------------------------------------

  describe "create_file_report/2" do
    test "creates a file report for the given user and media file" do
      media_file = MediaFilesFixtures.insert_media_file()
      user_id = Ecto.UUID.generate()

      assert {:ok, %FileReport{} = file_report} =
               ReportService.create_file_report(user_id, media_file)

      assert file_report.file_id == media_file.id
      assert file_report.user_id == user_id
    end

    test "returns an error changeset when the user id is nil" do
      media_file = MediaFilesFixtures.insert_media_file()

      assert {:error, changeset} = ReportService.create_file_report(nil, media_file)
      assert %{user_id: ["can't be blank"]} = errors_on(changeset)
    end
  end

  # ---------------------------------------------------------------------------
  # ban_file/1
  # ---------------------------------------------------------------------------

  describe "ban_file/1" do
    test "creates a reported file and deletes the media file and instances" do
      media_file = MediaFilesFixtures.insert_media_file()
      media_instance = MediaInstancesFixtures.insert_media_instance(%{file_id: media_file.id})
      encoded_hash = Base.encode64(media_file.hash)

      assert {:ok, file_id} = ReportService.ban_file(encoded_hash)
      assert file_id == media_file.id

      assert [%ReportedFile{hash: hash}] = FileReports.list_reported_file()
      assert hash == media_file.hash

      assert_raise Ecto.NoResultsError, fn -> MediaFiles.get_media_file!(media_file.id) end

      assert_raise Ecto.NoResultsError, fn ->
        MediaInstances.get_media_instance!(media_instance.id)
      end
    end

    test "returns ok without deleting the media file when the hash is already banned" do
      media_file = MediaFilesFixtures.insert_media_file()
      FileReportsFixtures.insert_reported_file(%{hash: media_file.hash})
      encoded_hash = Base.encode64(media_file.hash)

      assert {:ok, file_id} = ReportService.ban_file(encoded_hash)
      assert file_id == media_file.id
      assert MediaFiles.get_media_file!(media_file.id)
    end

    test "returns unprocessable entity when no media file exists for the hash" do
      encoded_hash = Base.encode64(:crypto.strong_rand_bytes(32))

      assert {:error, :unprocessable_entity} = ReportService.ban_file(encoded_hash)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_single_report/1
  # ---------------------------------------------------------------------------

  describe "delete_single_report/1" do
    test "deletes a file report and returns its id" do
      file_report = FileReportsFixtures.insert_file_report()

      assert {:ok, report_id} = ReportService.delete_single_report(file_report.id)
      assert report_id == file_report.id

      assert_raise Ecto.NoResultsError, fn -> FileReports.get_file_report!(file_report.id) end
    end

    test "returns an error when the file report does not exist" do
      assert {:error, "FileReport not found"} =
               ReportService.delete_single_report(Ecto.UUID.generate())
    end
  end

  # ---------------------------------------------------------------------------
  # remove_all_reports/1
  # ---------------------------------------------------------------------------

  describe "remove_all_reports/1" do
    test "returns ok status for each successfully removed report" do
      file_report_1 = FileReportsFixtures.insert_file_report()
      file_report_2 = FileReportsFixtures.insert_file_report()

      assert [
               %{report_id: report_id_1, status: "ok"},
               %{report_id: report_id_2, status: "ok"}
             ] = ReportService.remove_all_reports([file_report_1, file_report_2])

      assert report_id_1 == file_report_1.id
      assert report_id_2 == file_report_2.id
      assert FileReports.list_file_report() == []
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_reports_by_owner/1
  # ---------------------------------------------------------------------------

  describe "retrieve_reports_by_owner/1" do
    test "returns all reports for the given owner" do
      owner_id = Ecto.UUID.generate()
      file_report = FileReportsFixtures.insert_file_report(%{user_id: owner_id})
      FileReportsFixtures.insert_file_report()

      ids = ReportService.retrieve_reports_by_owner(owner_id) |> Enum.map(& &1.id)

      assert ids == [file_report.id]
    end

    test "returns an empty list when the owner has no reports" do
      FileReportsFixtures.insert_file_report()

      assert ReportService.retrieve_reports_by_owner(Ecto.UUID.generate()) == []
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_all_unique_reported_files/0
  # ---------------------------------------------------------------------------

  describe "retrieve_all_unique_reported_files/0" do
    test "returns each reported file id only once" do
      file_id = Ecto.UUID.generate()
      other_file_id = Ecto.UUID.generate()

      FileReportsFixtures.insert_file_report(%{file_id: file_id})
      FileReportsFixtures.insert_file_report(%{file_id: file_id})
      FileReportsFixtures.insert_file_report(%{file_id: other_file_id})

      file_ids = ReportService.retrieve_all_unique_reported_files()

      assert Enum.sort(file_ids) == Enum.sort([file_id, other_file_id])
    end

    test "returns an empty list when no reports exist" do
      assert ReportService.retrieve_all_unique_reported_files() == []
    end
  end

  # ---------------------------------------------------------------------------
  # update_reports/1
  # ---------------------------------------------------------------------------

  describe "update_reports/1" do
    test "deletes all reports for the given file id" do
      file_id = Ecto.UUID.generate()
      file_report_1 = FileReportsFixtures.insert_file_report(%{file_id: file_id})
      file_report_2 = FileReportsFixtures.insert_file_report(%{file_id: file_id})
      other_report = FileReportsFixtures.insert_file_report()

      assert [{:ok, %FileReport{}}, {:ok, %FileReport{}}] = ReportService.update_reports(file_id)

      assert_raise Ecto.NoResultsError, fn -> FileReports.get_file_report!(file_report_1.id) end
      assert_raise Ecto.NoResultsError, fn -> FileReports.get_file_report!(file_report_2.id) end
      assert FileReports.get_file_report!(other_report.id)
    end

    test "returns an empty list when no reports exist for the file id" do
      FileReportsFixtures.insert_file_report()

      assert ReportService.update_reports(Ecto.UUID.generate()) == []
    end
  end
end
