ExUnit.start()
Ecto.Adapters.SQL.Sandbox.mode(MediaServiceElx.Repo, :manual)
Mox.defmock(MediaServiceElx.MediaServiceMock, for: MediaServiceElx.MediaServiceBehaviour)
Mox.defmock(MediaServiceElx.ReportServiceMock, for: MediaServiceElx.ReportServiceBehaviour)
