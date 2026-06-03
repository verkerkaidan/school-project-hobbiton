defmodule MediaServiceElxWeb.ReportControllerTest do
  use MediaServiceElxWeb.ConnCase, async: true

  import Mox

  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.FileReports.FileReport

  setup :verify_on_exit!

  # ---------------------------------------------------------------------------
  # Helpers
  # ---------------------------------------------------------------------------

  defp build_media_file(overrides \\ %{}) do
    %MediaFile{
      id: "file-uuid-1",
      original: "binary-content",
      content_type: "image/png",
      content_flag: 2,
      hash: "abc123"
    }
    |> Map.merge(overrides)
  end

  defp build_file_report(overrides \\ %{}) do
    %FileReport{
      id: "report-uuid-1",
      file_id: "file-uuid-1",
      user_id: "user-uuid-1"
    }
    |> Map.merge(overrides)
  end

  # ---------------------------------------------------------------------------
  # create_file_report/2  POST /api/report/:user_id/:instance_id
  # ---------------------------------------------------------------------------

  describe "create_file_report/2" do
    test "returns report id when media file is found and report is created", %{conn: conn} do
      media_file = build_media_file()
      file_report = build_file_report()

      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-1" -> {:ok, media_file} end)
      expect(MediaServiceElx.ReportServiceMock, :create_file_report, fn "user-1", ^media_file -> {:ok, file_report} end)

      conn = post(conn, ~p"/api/report/user-1/inst-1")

      assert %{"report_id" => "report-uuid-1", "status" => "ok"} = json_response(conn, 200)
    end

    test "returns error when media file is not found", %{conn: conn} do
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "missing" -> {:error, :not_found} end)

      conn = post(conn, ~p"/api/report/user-1/missing")

      assert %{"status" => "error", "reason" => "MediaFile not found"} = json_response(conn, 200)
    end

    test "returns error when report creation fails", %{conn: conn} do
      media_file = build_media_file()

      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-1" -> {:ok, media_file} end)
      expect(MediaServiceElx.ReportServiceMock, :create_file_report, fn "user-1", ^media_file -> {:error, :duplicate} end)

      conn = post(conn, ~p"/api/report/user-1/inst-1")

      assert %{"status" => "error", "reason" => "duplicate"} = json_response(conn, 200)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_single_report/2  DELETE /api/report/:report_id
  # ---------------------------------------------------------------------------

  describe "delete_single_report/2" do
    test "returns ok with report id on successful deletion", %{conn: conn} do
      expect(MediaServiceElx.ReportServiceMock, :delete_single_report, fn "report-1" -> {:ok, "report-1"} end)

      conn = delete(conn, ~p"/api/report/report-1")

      assert %{"status" => "ok", "report_id" => "report-1"} = json_response(conn, 200)
    end

    test "returns error when report is not found", %{conn: conn} do
      expect(MediaServiceElx.ReportServiceMock, :delete_single_report, fn "missing" -> {:error, "FileReport not found"} end)

      conn = delete(conn, ~p"/api/report/missing")

      assert %{"status" => "error", "reason" => "FileReport not found"} = json_response(conn, 200)
    end
  end

  # ---------------------------------------------------------------------------
  # get_all_reported_files/2  GET /api/report
  # ---------------------------------------------------------------------------

  describe "get_all_reported_files/2" do
    test "returns full file data for all reported files", %{conn: conn} do
      media_file = build_media_file()

      expect(MediaServiceElx.ReportServiceMock, :retrieve_all_unique_reported_files, fn -> ["file-uuid-1"] end)
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_files_from_file_id, fn ["file-uuid-1"] -> %{files: [media_file]} end)

      conn = get(conn, ~p"/api/report")

      assert %{"data" => [file]} = json_response(conn, 200)
      assert file["id"] == "file-uuid-1"
      assert file["content_type"] == "image/png"
      assert file["file"] == Base.encode64(media_file.original)
    end

    test "returns empty data when no files are reported", %{conn: conn} do
      expect(MediaServiceElx.ReportServiceMock, :retrieve_all_unique_reported_files, fn -> [] end)
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_files_from_file_id, fn [] -> %{files: []} end)

      conn = get(conn, ~p"/api/report")

      assert %{"data" => []} = json_response(conn, 200)
    end
  end
end