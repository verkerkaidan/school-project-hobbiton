using System.Collections.Concurrent;
using System.Net.WebSockets;

var migrationRunner = new MigrationRunner(
    Environment.GetEnvironmentVariable("CHAT_DB_CONNECTION_STRING")
    ?? throw new InvalidOperationException("CHAT_DB_CONNECTION_STRING is not set")
);
migrationRunner.Run();

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddOpenApi();
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.SetIsOriginAllowed(origin =>
        {
            var uri = new Uri(origin);
            if (uri.Host == "localhost" || uri.Host == "apigateway-service") return true;
            return uri.Host == "student.inf.st.hsleiden.nl" ||
                   uri.Host.EndsWith(".student.inf.st.hsleiden.nl");
        })
        .AllowAnyHeader()
        .AllowAnyMethod();
    });
});

var app = builder.Build();
app.UseCors();
app.UseWebSockets();

var lobbies = new ConcurrentDictionary<string, List<WebSocket>>();
var userSockets = new ConcurrentDictionary<string, WebSocket>();
var notificationSockets = new ConcurrentDictionary<string, WebSocket>();
var db = new DatabaseConnection();

ChatWebSocketHandler.Map(app, lobbies, userSockets, notificationSockets, db);
NotificationWebSocketHandler.Map(app, notificationSockets, db);
ChatEndpoints.Map(app, userSockets, db);

app.Run();