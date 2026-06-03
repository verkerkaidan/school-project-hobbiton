if Code.ensure_loaded?(Mox) do
  Mox.defmock(MediaServiceElx.MediaServiceMock, for: MediaServiceElx.MediaServiceBehaviour)
  Mox.defmock(MediaServiceElx.ReportServiceMock, for: MediaServiceElx.ReportServiceBehaviour)
end