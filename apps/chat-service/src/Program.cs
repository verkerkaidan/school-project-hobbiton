using System.Collections.Concurrent;
using System.Net.WebSockets;

Microsoft.IdentityModel.Logging.IdentityModelEventSource.ShowPII = true;

var migrationRunner = new MigrationRunner(
    Environment.GetEnvironmentVariable("CHAT_DB_CONNECTION_STRING")
    ?? throw new InvalidOperationException("CHAT_DB_CONNECTION_STRING is not set")
);
migrationRunner.Run();

var chatAllowedOrigins = GetAllowedOrigins(
    Environment.GetEnvironmentVariable("CHAT_ALLOWED_ORIGINS")
    ?? "https://hobbiton.student.inf.st.hsleiden.nl,http://localhost:23738"
);

// ... rest of your app startup
var builder = WebApplication.CreateBuilder(args);
builder.Services.AddOpenApi();
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.SetIsOriginAllowed(origin =>
        {
            var normalizedOrigin = NormalizeOrigin(origin);
            return normalizedOrigin != null && chatAllowedOrigins.Contains(normalizedOrigin);
        })
        .AllowAnyHeader()
        .AllowAnyMethod();
    });
});

builder.Services.AddSingleton<JwtUtil>();

var app = builder.Build();
app.UseCors();
app.UseWebSockets();
app.UseMiddleware<JwtMiddleware>();

var lobbies = new ConcurrentDictionary<string, List<WebSocket>>();
var userSockets = new ConcurrentDictionary<string, WebSocket>();
var notificationSockets = new ConcurrentDictionary<string, WebSocket>();
var db = new DatabaseConnection();

ChatWebSocketHandler.Map(app, lobbies, userSockets, notificationSockets, db);
NotificationWebSocketHandler.Map(app, notificationSockets, db);
ChatEndpoints.Map(app, userSockets, db);

app.Run();

static HashSet<string> GetAllowedOrigins(string csvOrigins)
{
    return csvOrigins
        .Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
        .Select(NormalizeOrigin)
        .Where(origin => origin != null)
        .Cast<string>()
        .ToHashSet(StringComparer.OrdinalIgnoreCase);
}

static string? NormalizeOrigin(string? origin)
{
    if (string.IsNullOrWhiteSpace(origin))
    {
        return null;
    }

    if (!Uri.TryCreate(origin, UriKind.Absolute, out var uri))
    {
        return null;
    }

    return $"{uri.Scheme}://{uri.Authority}";
}