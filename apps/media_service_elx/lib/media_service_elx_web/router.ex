defmodule MediaServiceElxWeb.Router do
  use MediaServiceElxWeb, :router

  alias MediaServiceElxWeb.Plugs.JWTPlug

  # ------------------------------------------------------------------
  # Pipelines
  # ------------------------------------------------------------------

  pipeline :api do
    plug :accepts, ["json"]
  end

  pipeline :authenticated do
    plug :accepts, ["json"]
    plug JWTPlug
  end

  # ------------------------------------------------------------------
  # Routes
  # ------------------------------------------------------------------

  scope "/api/media", MediaServiceElxWeb do
    pipe_through :api

    put "/", MediaController, :update_content_flag
    get "/", MediaController, :get_unassigned_media_files
  end

  scope "/api/media", MediaServiceElxWeb do
    pipe_through :authenticated

    post "/",                  MediaController, :upload_media
    get "/file",               MediaController, :retrieve_media
    delete "/file/:instance_id", MediaController, :delete_instance
    delete "/admin",           MediaController, :delete_media_file
  end

  scope "/api/report", MediaServiceElxWeb do
    pipe_through :authenticated

    post "/:user_id/:instance_id", ReportController, :create_file_report
    delete "/:report_id",          ReportController, :delete_single_report
    get "/",                       ReportController, :get_all_reported_files
  end

  scope "/api/user", MediaServiceElxWeb do
    pipe_through :authenticated

    delete "/:owner_id",           UserController, :delete_owner
    get "/:owner_id",              UserController, :retrieve_media_by_owner
    get "/reports/:owner_id",      UserController, :retrieve_reports_by_owner
  end

  # ------------------------------------------------------------------
  # Dev tooling
  # ------------------------------------------------------------------

  if Application.compile_env(:media_service_elx, :dev_routes) do
    import Phoenix.LiveDashboard.Router

    scope "/dev" do
      pipe_through [:fetch_session, :protect_from_forgery]

      live_dashboard "/dashboard", metrics: MediaServiceElxWeb.Telemetry
      forward "/mailbox", Plug.Swoosh.MailboxPreview
    end
  end
end