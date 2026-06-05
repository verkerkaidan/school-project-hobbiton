defmodule MediaServiceElxWeb.MediaControllerTest do
  use MediaServiceElxWeb.ConnCase, async: false

  import Mox

  alias MediaServiceElx.MediaFiles.MediaFile

  # Ensure mocks are verified after each test
  setup :verify_on_exit!

  # ---------------------------------------------------------------------------
  # Helpers
  # ---------------------------------------------------------------------------

  defp build_media_file(overrides \\ %{}) do
    %MediaFile{
      original: "binary-content",
      content_type: "image/png",
      content_flag: :unassigned,
      hash: "abc123"
    }
    |> Map.merge(overrides)
  end

  # ---------------------------------------------------------------------------
  # upload_media/2  POST /api/media
  # ---------------------------------------------------------------------------

  describe "upload_media/2" do
    test "returns ok status for each successfully uploaded file", %{conn: conn} do
      upload = %Plug.Upload{path: "../support/files/test.png", filename: "test.png", content_type: "image/png"}

      MediaServiceElx.MediaServiceMock
      |> expect(:upload_media_file, fn _upload, "owner-1" -> {:ok, "instance-uuid-1"} end)
      |> expect(:upload_media_file, fn _upload, "owner-1" -> {:ok, "instance-uuid-2"} end)

      conn =
        post(conn, ~p"/api/media", %{
          "owner_id" => "owner-1",
          "files" => [upload, upload]
        })

      assert %{"results" => results} = json_response(conn, 200)
      assert [%{"id" => "instance-uuid-1", "status" => "ok"}, %{"id" => "instance-uuid-2", "status" => "ok"}] = results
    end

    test "returns banned error when service returns {:error, :banned}", %{conn: conn} do
      upload = %Plug.Upload{path: "../support/files/test.png", filename: "test.png", content_type: "image/png"}

      expect(MediaServiceElx.MediaServiceMock, :upload_media_file, fn _upload, _owner -> {:error, :banned} end)

      conn =
        post(conn, ~p"/api/media", %{
          "owner_id" => "owner-1",
          "files" => [upload]
        })

      assert %{"results" => [%{"status" => "error", "reason" => "banned"}]} = json_response(conn, 200)
    end

    test "returns invalid file error for any other service error", %{conn: conn} do
      upload = %Plug.Upload{path: "../support/files/test.png", filename: "test.png", content_type: "image/png"}

      expect(MediaServiceElx.MediaServiceMock, :upload_media_file, fn _upload, _owner -> {:error, :some_reason} end)

      conn =
        post(conn, ~p"/api/media", %{
          "owner_id" => "owner-1",
          "files" => [upload]
        })

      assert %{"results" => [%{"status" => "error", "reason" => "invalid file"}]} = json_response(conn, 200)
    end

    test "handles a mix of successful and failed uploads", %{conn: conn} do
      upload = %Plug.Upload{path: "../support/files/test.png", filename: "test.png", content_type: "image/png"}

      MediaServiceElx.MediaServiceMock
      |> expect(:upload_media_file, fn _upload, _owner -> {:ok, "instance-uuid-1"} end)
      |> expect(:upload_media_file, fn _upload, _owner -> {:error, :banned} end)

      conn =
        post(conn, ~p"/api/media", %{
          "owner_id" => "owner-1",
          "files" => [upload, upload]
        })

      assert %{"results" => [%{"status" => "ok"}, %{"status" => "error", "reason" => "banned"}]} =
               json_response(conn, 200)
    end
  end

  # ---------------------------------------------------------------------------
  # retrieve_media/2  GET /api/media/file
  # ---------------------------------------------------------------------------

  describe "retrieve_media/2" do
    test "returns encoded file data for each valid instance", %{conn: conn} do
      media_file = build_media_file()

      expect(MediaServiceElx.MediaServiceMock, :retrieve_media_file, fn "inst-1" -> {:ok, media_file} end)

      conn = get(conn, ~p"/api/media/file", %{"instances" => ["inst-1"]})

      assert [result] = json_response(conn, 200)
      assert result["relevantId"] == "inst-1"
      assert result["fileData"] == Base.encode64(media_file.original)
      assert result["contentType"] == "image/png"
      assert result["contentFlag"] == "unassigned"
    end

    test "handles a mix of found and missing instances", %{conn: conn} do
      media_file = build_media_file()

      MediaServiceElx.MediaServiceMock
      |> expect(:retrieve_media_file, fn "inst-1" -> {:ok, media_file} end)
      |> expect(:retrieve_media_file, fn "inst-2" -> {:error, "MediaInstance not found"} end)

      conn = get(conn, ~p"/api/media/file", %{"instances" => ["inst-1", "inst-2"]})

      assert [found, missing] = json_response(conn, 200)
      assert found["relevantId"] == "inst-1"
      assert missing == nil
    end
  end

  # ---------------------------------------------------------------------------
  # delete_instance/2  DELETE /api/media/file/:instance_id
  # ---------------------------------------------------------------------------

  describe "delete_instance/2" do
    test "returns the instance id on successful deletion", %{conn: conn} do
      expect(MediaServiceElx.MediaServiceMock, :delete_single_instance, fn "inst-1" -> {:ok, "inst-1"} end)

      conn = delete(conn, ~p"/api/media/file/inst-1")

      assert %{"id" => "inst-1"} = json_response(conn, 200)
    end

    test "returns 422 with error reason when deletion fails", %{conn: conn} do
      expect(MediaServiceElx.MediaServiceMock, :delete_single_instance, fn "inst-bad" -> {:error, :not_found} end)

      conn = delete(conn, ~p"/api/media/file/inst-bad")

      assert %{"error" => "not_found"} = json_response(conn, 422)
    end
  end

  # ---------------------------------------------------------------------------
  # update_content_flag/2  PUT /api/media
  # ---------------------------------------------------------------------------

  describe "update_content_flag/2" do
    test "returns ok status for each successfully updated file", %{conn: conn} do
      # content_flag("SAFE") => 0
      expect(MediaServiceElx.MediaServiceMock, :update_content_flag, fn 0, "hash-1", "looks good" ->
        {:ok, "file-id-1"}
      end)

      conn =
        conn
        |> put_req_header("content-type", "application/json")
        |> put(~p"/api/media", Jason.encode!([%{"contentFlag" => "SAFE", "hash" => "hash-1", "note" => "looks good"}]))

      assert %{"results" => [%{"file_id" => "file-id-1", "status" => "ok"}]} = json_response(conn, 200)
    end

    test "uses empty string as default note when note is absent", %{conn: conn} do
      # content_flag("RISK") => 1
      expect(MediaServiceElx.MediaServiceMock, :update_content_flag, fn 1, "hash-1", "" -> {:ok, "file-id-1"} end)

      conn =
        conn
        |> put_req_header("content-type", "application/json")
        |> put(~p"/api/media", Jason.encode!([%{"contentFlag" => "RISK", "hash" => "hash-1"}]))

      assert %{"results" => [%{"status" => "ok"}]} = json_response(conn, 200)
    end

    test "returns error entry when service call fails", %{conn: conn} do
      # content_flag("UNASSIGNED") => 2
      expect(MediaServiceElx.MediaServiceMock, :update_content_flag, fn 2, _hash, _note ->
        {:error, :invalid_flag}
      end)

      conn =
        conn
        |> put_req_header("content-type", "application/json")
        |> put(~p"/api/media", Jason.encode!([%{"contentFlag" => "UNASSIGNED", "hash" => "hash-1"}]))

      assert %{"results" => [%{"status" => "error", "reason" => "invalid_flag"}]} = json_response(conn, 200)
    end
  end

  # ---------------------------------------------------------------------------
  # get_unassigned_media_files/2  GET /api/media
  # ---------------------------------------------------------------------------

  describe "get_unassigned_media_files/2" do
    test "returns base64-encoded file and hash for each unassigned media file", %{conn: conn} do
      media_file = build_media_file(%{content_flag: :unassigned})

      expect(MediaServiceElx.MediaServiceMock, :retrieve_unassigned, fn -> [media_file] end)

      conn = get(conn, ~p"/api/media")

      assert [result] = json_response(conn, 200)
      assert result["file"] == Base.encode64(media_file.original)
      assert result["hash"] == Base.encode64(media_file.hash)
    end

    test "returns empty list when no unassigned files exist", %{conn: conn} do
      expect(MediaServiceElx.MediaServiceMock, :retrieve_unassigned, fn -> [] end)

      conn = get(conn, ~p"/api/media")

      assert [] = json_response(conn, 200)
    end
  end

  # ---------------------------------------------------------------------------
  # delete_media_file/2  DELETE /api/media/admin/:file_id
  # ---------------------------------------------------------------------------

  describe "delete_media_file/2" do
    test "returns file id on successful ban", %{conn: conn} do
      expect(MediaServiceElx.ReportServiceMock, :ban_file, fn "file-1" -> {:ok, "file-1"} end)

      conn = delete(conn, ~p"/api/media/admin", %{hash: "file-1"})

      assert %{"id" => "file-1"} = json_response(conn, 200)
    end

    test "returns 422 with generic error message when ban fails", %{conn: conn} do
      expect(MediaServiceElx.ReportServiceMock, :ban_file, fn "file-bad" -> {:error, :not_found} end)

      conn = delete(conn, ~p"/api/media/admin", %{hash: "file-bad"})

      assert %{"error" => "Invalid id"} = json_response(conn, 422)
    end
  end
end