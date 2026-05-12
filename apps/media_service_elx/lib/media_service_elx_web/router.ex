defmodule MediaServiceElxWeb.Router do
  use MediaServiceElxWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
  end

  scope "/api/media", MediaServiceElxWeb do
    pipe_through :api

    post "/:ownerId", MediaController, :upload_media
    get "/file/:instanceId", MediaController, :retrieve_media
    get "/owner/:ownerId", MediaController, :retrieve_media_by_owner
    delete "/file/:instanceId", MediaController, :delete_instance
    delete "/owner/:ownerId", MediaController, :delete_owner
    patch "", MediaController, :update_content_flag
    get "", MediaController, :get_unassigned_media_files
    get "/admin/:instanceId", MediaController, :get_complete_media_file
    delete "admin/:fileId", MediaController, :delete_media_file
  end

  scope "/api/report", MediaServiceElxWeb do
    pipe_through :api

    post "/:userId/:instanceId", ReportController, :create_file_report
    get "", ReportController, :get_all_reported_files
  end

  # Enable LiveDashboard and Swoosh mailbox preview in development
  if Application.compile_env(:media_service_elx, :dev_routes) do
    # If you want to use the LiveDashboard in production, you should put
    # it behind authentication and allow only admins to access it.
    # If your application does not have an admins-only section yet,
    # you can use Plug.BasicAuth to set up some basic authentication
    # as long as you are also using SSL (which you should anyway).
    import Phoenix.LiveDashboard.Router

    scope "/dev" do
      pipe_through [:fetch_session, :protect_from_forgery]

      live_dashboard "/dashboard", metrics: MediaServiceElxWeb.Telemetry
      forward "/mailbox", Plug.Swoosh.MailboxPreview
    end
  end
end
