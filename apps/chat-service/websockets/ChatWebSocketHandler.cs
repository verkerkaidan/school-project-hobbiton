using System.Collections.Concurrent;
using System.Net.WebSockets;

public static class ChatWebSocketHandler
{
    public static void Map(
        WebApplication app,
        ConcurrentDictionary<string, List<WebSocket>> lobbies,
        ConcurrentDictionary<string, WebSocket> userSockets,
        ConcurrentDictionary<string, WebSocket> notificationSockets,
        DatabaseConnection db)
    {
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

            if (!db.isUserAllowed(chatId, userId))
            {
                context.Response.StatusCode = 403;
                return;
            }

            var webSocket = await context.WebSockets.AcceptWebSocketAsync();
            userSockets[$"{userId}:{chatId}"] = webSocket;

            var sockets = lobbies.GetOrAdd(chatId, _ => new List<WebSocket>());
            lock (sockets) sockets.Add(webSocket);

            var history = db.GetChatHistory(chatId);
            foreach (var msgBytes in history)
            {
                await webSocket.SendAsync(
                    new ArraySegment<byte>(msgBytes),
                    WebSocketMessageType.Binary,
                    endOfMessage: true,
                    CancellationToken.None);
            }

            await HandleAsync(webSocket, sockets, chatId, userId, db, context, notificationSockets, userSockets);
        });
    }

    private static async Task HandleAsync(
        WebSocket webSocket,
        List<WebSocket> sockets,
        string chatId,
        string userId,
        DatabaseConnection db,
        HttpContext context,
        ConcurrentDictionary<string, WebSocket> notificationSockets,
        ConcurrentDictionary<string, WebSocket> userSockets)
    {
        var buffer = new byte[1024 * 4];

        try
        {
            while (webSocket.State == WebSocketState.Open)
            {
                if (!db.isUserAllowed(chatId, userId))
                {
                    context.Response.StatusCode = 403;
                    return;
                }

                var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);

                if (result.MessageType == WebSocketMessageType.Close)
                {
                    await webSocket.CloseAsync(WebSocketCloseStatus.NormalClosure, "Closing", CancellationToken.None);
                    break;
                }

                var msgBytes = buffer[..result.Count];
                db.SaveMessage(chatId, msgBytes);
                db.MarkChatAsRead(userId, chatId);

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

                        // Find which user this socket belongs to and mark as read
                        var recipientKey = userSockets.FirstOrDefault(kvp => kvp.Value == socket).Key;
                        if (recipientKey != null)
                        {
                            var recipientUserId = recipientKey[..recipientKey.LastIndexOf(':')];
                            db.MarkChatAsRead(recipientUserId, chatId);
                        }
                    }
                }

                await NotificationHelper.BroadcastUnreadCounts(chatId, userId, db, notificationSockets);
            }
        }
        finally
        {
            lock (sockets) sockets.Remove(webSocket);
            userSockets.TryRemove($"{userId}:{chatId}", out _);
        }
    }
}