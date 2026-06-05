using System.Collections.Concurrent;
using System.Net.WebSockets;

var migrationRunner = new MigrationRunner(
    Environment.GetEnvironmentVariable("CHAT_DB_CONNECTION_STRING")
    ?? throw new InvalidOperationException("CHAT_DB_CONNECTION_STRING is not set")
);
migrationRunner.Run();

// ... rest of your app startup
var builder = WebApplication.CreateBuilder(args);
builder.Services.AddOpenApi(); // add this

builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.SetIsOriginAllowed(origin =>
        {
            var uri = new Uri(origin);

            // localhost (any port)
            if (uri.Host == "localhost" || uri.Host == "apigateway-service")
                return true;

            // allow all student subdomains
            return uri.Host == "student.inf.st.hsleiden.nl" ||
                   uri.Host.EndsWith(".student.inf.st.hsleiden.nl");
        })
        .AllowAnyHeader()
        .AllowAnyMethod();
    });
});

var app = builder.Build();

app.UseCors();
var lobbies = new ConcurrentDictionary<string, List<WebSocket>>();
var userSockets = new ConcurrentDictionary<string, WebSocket>();
var db = new DatabaseConnection();

app.UseWebSockets();
// URL: /ws/{chatId}?userId={userId}
app.Map("/ws/{chatId}", async context =>
{
    var chatId = context.Request.RouteValues["chatId"]?.ToString();
    var userId = context.Request.Query["userId"].ToString();

    if (string.IsNullOrEmpty(chatId) || string.IsNullOrEmpty(userId))
    {
        context.Response.StatusCode = 400;
        return;
    }

    if (!context.WebSockets.IsWebSocketRequest)
    {
        context.Response.StatusCode = 400;
        return;
    }

    // Reject users not in allowed_users for this chat
    if (!db.isUserAllowed(chatId, userId))
    {
        context.Response.StatusCode = 403;
        return;
    }

    Console.WriteLine("instating websocket");
    var webSocket = await context.WebSockets.AcceptWebSocketAsync();
    userSockets[$"{userId}:{chatId}"] = webSocket;

    var sockets = lobbies.GetOrAdd(chatId, _ => new List<WebSocket>());
    lock (sockets) sockets.Add(webSocket);

    // Replay history from the database
    var history = db.GetChatHistory(chatId);
    foreach (var msgBytes in history)
    {
        await webSocket.SendAsync(
            new ArraySegment<byte>(msgBytes),
            WebSocketMessageType.Binary,
            endOfMessage: true,
            CancellationToken.None);
    }

    await HandleWebSocket(webSocket, sockets, chatId, userId, db, context);
});

app.MapPost("/create-chat", (ChatModel chatModel) =>
{
    var chatId = Guid.NewGuid();
    db.createChat(chatId, chatModel.title);
    db.AllowUsers(chatId.ToString(), chatModel.userIds);
    return Results.Ok(new { chatId });
});

app.MapGet("/get-chats/{userId}", (string userId) =>
{
    var chats = db.getAllChatsFromUser(userId);
    return Results.Ok(chats);
});

app.MapGet("/get-chat-title/{chatId}", (string chatId) =>
{
    var title = db.getTitleFromChatId(chatId);
    return Results.Ok(title);
});

app.MapPut("/update-title", (TitleModel titleModel) =>
{
    return db.alterTitleFromChatId(titleModel);
});

app.MapPut("/add-user-to-chat", (AlterUserModel alterUserModel) =>
{
    bool wasAdded = db.addUserToChat(alterUserModel);
    return Results.Ok(wasAdded);
});

app.MapPut("/delete-user-from-chat", async (AlterUserModel alterUserModel) =>
{
    bool wasRemoved = db.removeUserFromChat(alterUserModel);
    
    if (wasRemoved)
    {
        // Close the WebSocket connection for the removed user
        var key = $"{alterUserModel.userId}:{alterUserModel.chatId}";
        if (userSockets.TryRemove(key, out var webSocket))
        {
            await webSocket.CloseAsync(
                WebSocketCloseStatus.NormalClosure,
                "Removed from chat",
                CancellationToken.None);
        }
    }
    
    return Results.Ok(wasRemoved);
});

app.MapGet("/allowed-users/{chatId}", (string chatId)=>
{
    return db.chatUserList(chatId);
});



async Task HandleWebSocket(
    WebSocket webSocket,
    List<WebSocket> sockets,
    string chatId,
    string userId,
    DatabaseConnection db,
    HttpContext context)
{
    var buffer = new byte[1024 * 4];

    try
    {
        while (webSocket.State == WebSocketState.Open)
        {
            // Reject users not in allowed_users for this chat
            if (!db.isUserAllowed(chatId, userId))
            {
                context.Response.StatusCode = 403;
                return;
            }
            var result = await webSocket.ReceiveAsync(
                new ArraySegment<byte>(buffer), CancellationToken.None);

            if (result.MessageType == WebSocketMessageType.Close)
            {
                await webSocket.CloseAsync(
                    WebSocketCloseStatus.NormalClosure, "Closing", CancellationToken.None);
                break;
            }

            var msgBytes = buffer[..result.Count];

            // Persist to database
            db.SaveMessage(chatId, msgBytes);

            // Broadcast to everyone else in the lobby
            List<WebSocket> snapshot;
            lock (sockets) snapshot = sockets.ToList();

            foreach (var socket in snapshot)
            {
                if (socket != webSocket && socket.State == WebSocketState.Open)
                {
                    await socket.SendAsync(
                        new ArraySegment<byte>(msgBytes),
                        WebSocketMessageType.Binary,
                        endOfMessage: true,
                        CancellationToken.None);
                }
            }
        }
    }
    finally
    {
        lock (sockets) sockets.Remove(webSocket);
        userSockets.TryRemove($"{userId}:{chatId}", out _);
    }
}

app.Run();