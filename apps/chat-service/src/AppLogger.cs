using Serilog;
using Serilog.Sinks.Grafana.Loki;

public static class AppLogger
{
    public static void Init()
    {
        var config = new LoggerConfiguration()
            .MinimumLevel.Information()
            .Enrich.FromLogContext()
            .Enrich.WithMachineName()
            .WriteTo.Console();

        var lokiUrl = Environment.GetEnvironmentVariable("LOKI_URL");
        if (!string.IsNullOrEmpty(lokiUrl))
        {
            config.WriteTo.GrafanaLoki(
                lokiUrl,
                labels:
                [
                    new LokiLabel { Key = "app", Value = "chat-service" },
                    new LokiLabel { Key = "env", Value = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") ?? "production" },
                ]
            );
        }

        Log.Logger = config.CreateLogger();
    }

    public static void Info(string message)  => Serilog.Log.Information(message);
    public static void Warn(string message)  => Serilog.Log.Warning(message);
    public static void Error(string message) => Serilog.Log.Error(message);
}