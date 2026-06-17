using System.Collections.Concurrent;
using System.Net.WebSockets;

public static class NotificationHelper
{
    public static async Task SendUnreadCounts(WebSocket webSocket, object counts)
    {
        var message = System.Text.Json.JsonSerializer.Serialize(new
        {
            type = "unread_counts",
            data = counts
        });
        var bytes = System.Text.Encoding.UTF8.GetBytes(message);
        await webSocket.SendAsync(
            new ArraySegment<byte>(bytes),
            WebSocketMessageType.Text,
            endOfMessage: true,
            CancellationToken.None);
    }

    public static async Task BroadcastUnreadCounts(
        string chatId,
        string senderUserId,
        IDatabaseConnection db,
        ConcurrentDictionary<string, WebSocket> notificationSockets)
    {
        var allowedUsers = db.chatUserList(chatId);
        foreach (var user in allowedUsers)
        {
            if (user == senderUserId) continue;

            if (notificationSockets.TryGetValue(user, out var notifSocket) && notifSocket.State == WebSocketState.Open)
            {
                try
                {
                    await SendUnreadCounts(notifSocket, db.GetUnreadCountsForUser(user));
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error sending notification to user {user}: {ex.Message}");
                }
            }
        }
    }
}