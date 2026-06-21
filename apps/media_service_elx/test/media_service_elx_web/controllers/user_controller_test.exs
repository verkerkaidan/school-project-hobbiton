defmodule MediaServiceElxWeb.UserControllerTest do
  use MediaServiceElxWeb.ConnCase, async: false

  import Mox

  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaInstances.MediaInstance
  alias MediaServiceElx.FileReports.FileReport

  setup %{conn: conn} do
    {:ok, conn: authed_conn(conn)}
  end
  setup :verify_on_exit!

  # ---------------------------------------------------------------------------
  # Helpers
  # ---------------------------------------------------------------------------

  defp build_instance(overrides \\ %{}) do
    %MediaInstance{
      id: "inst-uuid-1"
    }
    |> Map.merge(overrides)
  end

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
  # retrieve_media_by_owner/2  GET /api/user/:owner_id
  # ---------------------------------------------------------------------------

  describe "retrieve_media_by_owner/2" do
    test "returns partial file data for each instance owned by the user", %{conn: conn} do
      instance = build_instance()
      media_file = build_media_file()

      expect(MediaServiceElx.MediaServiceMock, :retrieve_instances_by_owner, fn "owner-1" -> [instance] end)
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-uuid-1" -> {:ok, media_file} end)

      conn = get(conn, ~p"/api/user/owner-1")

      assert %{"results" => [result]} = json_response(conn, 200)
      assert result["instance_id"] == "inst-uuid-1"
      assert result["status"] == "ok"
      assert result["media_file"]["fileData"] == Base.encode64(media_file.original)
      assert result["media_file"]["contentType"] == "image/png"
      assert result["media_file"]["contentFlag"] == 2
    end

    test "returns error entry when a media file cannot be retrieved", %{conn: conn} do
      instance = build_instance()

      expect(MediaServiceElx.MediaServiceMock, :retrieve_instances_by_owner, fn "owner-1" -> [instance] end)
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-uuid-1" -> {:error, :not_found} end)

      conn = get(conn, ~p"/api/user/owner-1")

      assert %{"results" => [%{"status" => "error", "reason" => "not_found"}]} = json_response(conn, 200)
    end

    test "returns empty results when owner has no instances", %{conn: conn} do
      expect(MediaServiceElx.MediaServiceMock, :retrieve_instances_by_owner, fn "owner-1" -> [] end)

      conn = get(conn, ~p"/api/user/owner-1")

      assert %{"results" => []} = json_response(conn, 200)
    end

    test "handles a mix of successful and failed retrievals", %{conn: conn} do
      instance_1 = build_instance(%{id: "inst-uuid-1"})
      instance_2 = build_instance(%{id: "inst-uuid-2"})
      media_file = build_media_file()

      expect(MediaServiceElx.MediaServiceMock, :retrieve_instances_by_owner, fn "owner-1" -> [instance_1, instance_2] end)
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-uuid-1" -> {:ok, media_file} end)
      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-uuid-2" -> {:error, :not_found} end)

      conn = get(conn, ~p"/api/user/owner-1")

      assert %{"results" => [ok_result, error_result]} = json_response(conn, 200)
      assert ok_result["status"] == "ok"
      assert error_result["status"] == "error"
    end
  end

  # ---------------------------------------------------------------------------
  # delete_owner/2  DELETE /api/user/:owner_id
  # ---------------------------------------------------------------------------

  describe "delete_owner/2" do
    test "deletes all instances and reports for an owner", %{conn: conn} do
      instances = [build_instance()]
      reports = [build_file_report()]

      expect(MediaServiceElx.MediaServiceMock, :retrieve_instances_by_owner, fn "owner-1" -> instances end)
      expect(MediaServiceElx.MediaServiceMock, :delete_single_instance, fn _id ->
        {:ok, "inst-uuid-1"}
      end)
      expect(MediaServiceElx.ReportServiceMock, :retrieve_reports_by_owner, fn "owner-1" -> reports end)
      expect(MediaServiceElx.ReportServiceMock, :remove_all_reports, fn ^reports ->
        {:ok, [%{report_id: "report-uuid-1", status: "ok"}]}
      end)

      conn = delete(conn, ~p"/api/user/owner-1")

      assert %{"instances" => instances_resp, "reports" => reports_resp} = json_response(conn, 200)
      assert [%{"id" => "inst-uuid-1", "status" => "deleted"}] = instances_resp
      assert %{"status" => "deleted", "result" => [%{"report_id" => "report-uuid-1", "status" => "ok"}]} = reports_resp
    end

    test "returns empty results when owner has no instances or reports", %{conn: conn} do
      expect(MediaServiceElx.MediaServiceMock, :retrieve_instances_by_owner, fn "owner-1" -> [] end)
      expect(MediaServiceElx.ReportServiceMock, :retrieve_reports_by_owner, fn "owner-1" -> [] end)
      expect(MediaServiceElx.ReportServiceMock, :remove_all_reports, fn [] -> {:ok, []} end)

      conn = delete(conn, ~p"/api/user/owner-1")

      assert %{"instances" => [], "reports" => reports_resp} = json_response(conn, 200)
      assert %{"status" => "deleted", "result" => []} = reports_resp
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_reports_by_owner/2  GET /api/user/reports/:owner_id
  # NOTE: controller has a typo — `ower_id` instead of `owner_id` — this will
  # crash at runtime until fixed. Tests are written against the intended behaviour.
  # ---------------------------------------------------------------------------

  describe "retrieve_reports_by_owner/2" do
    test "returns all reports for a given owner", %{conn: conn} do
      reports = [build_file_report()]

      expect(MediaServiceElx.ReportServiceMock, :retrieve_reports_by_owner, fn "owner-1" -> reports end)

      conn = get(conn, ~p"/api/user/reports/owner-1")

      assert %{"reports" => [report]} = json_response(conn, 200)
      assert report["id"] == "report-uuid-1"
    end

    test "returns empty list when owner has no reports", %{conn: conn} do
      expect(MediaServiceElx.ReportServiceMock, :retrieve_reports_by_owner, fn "owner-1" -> [] end)

      conn = get(conn, ~p"/api/user/reports/owner-1")

      assert %{"reports" => []} = json_response(conn, 200)
    end
  end
end