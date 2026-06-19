using System.Collections.Concurrent;
using System.Net.WebSockets;

public static class NotificationWebSocketHandler
{
    public static void Map(
        WebApplication app,
        ConcurrentDictionary<string, WebSocket> notificationSockets,
        IDatabaseConnection db)
    {
        app.Map("/ws/notifications/{userId}", async context =>
        {
            var userId = context.Request.RouteValues["userId"]?.ToString();

            if (string.IsNullOrEmpty(userId))
            {
                AppLogger.Warn($"userId was empty for fetching notifications");
                context.Response.StatusCode = 400;
                return;
            }
            if (!context.WebSockets.IsWebSocketRequest)
            {
                AppLogger.Warn($"Websocket connection not connected");
                context.Response.StatusCode = 400;
                return;
            }

            var webSocket = await context.WebSockets.AcceptWebSocketAsync();
            notificationSockets[userId] = webSocket;

            try
            {
                await HandleAsync(webSocket, userId, db);
            }
            finally
            {
                notificationSockets.TryRemove(userId, out _);
                webSocket.Dispose();
            }
        });
    }

    public static async Task HandleAsync(
        WebSocket webSocket,
        string userId,
        IDatabaseConnection db)
    {
        await NotificationHelper.SendUnreadCounts(webSocket, db.GetUnreadCountsForUser(userId));

        var buffer = new byte[1024 * 4];
        while (webSocket.State == WebSocketState.Open)
        {
            var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);

            if (result.MessageType == WebSocketMessageType.Close)
            {
                AppLogger.Info($"Closing notification websocket");
                await webSocket.CloseAsync(WebSocketCloseStatus.NormalClosure, "Closing", CancellationToken.None);
                break;
            }

            try
            {
                var msgString = System.Text.Encoding.UTF8.GetString(buffer, 0, result.Count);
                var json = System.Text.Json.JsonDocument.Parse(msgString);
                var root = json.RootElement;

                if (!root.TryGetProperty("type", out var typeElement)) continue;
                var type = typeElement.GetString();

                if (type == "mark_chat_as_read")
                {
                    if (root.TryGetProperty("chatId", out var chatIdElement))
                    {
                        var chatId = chatIdElement.GetString();
                        if (!string.IsNullOrEmpty(chatId))
                        {
                            db.MarkChatAsRead(userId, chatId);
                            await NotificationHelper.SendUnreadCounts(webSocket, db.GetUnreadCountsForUser(userId));
                        }
                    }
                }
                else if (type == "mark_all_as_read")
                {
                    db.MarkAllChatsAsRead(userId);
                    await NotificationHelper.SendUnreadCounts(webSocket, db.GetUnreadCountsForUser(userId));
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error parsing notification message: {ex.Message}");
            }
        }
    }
}