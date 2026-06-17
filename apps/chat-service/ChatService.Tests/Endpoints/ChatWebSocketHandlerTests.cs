using System.Collections.Concurrent;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using System.Linq;
using System.Collections.Generic;
using System;
using Xunit;
public class ChatWebSocketHandlerTests
{
    private static readonly string ChatId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    private static readonly string User1 = "3fa85f64-5717-4562-b3fc-2c963f66afa1";
    private static readonly string User2 = "3fa85f64-5717-4562-b3fc-2c963f66afa2";
    private static readonly string User3 = "3fa85f64-5717-4562-b3fc-2c963f66afa3";

    // ── Happy flows ──────────────────────────────────────────────────────────

    [Fact]
    public async Task User1SendsMessage_User2Receives()
    {
        // Arrange
        var (user1Socket, user2Socket, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        var payload = Encoding.UTF8.GetBytes("hello from user1");
        user1Socket.EnqueueIncoming(payload);
        user1Socket.EnqueueClose();

        // Act
        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        // Assert
        Assert.Single(user2Socket.SentMessages);
        Assert.Equal(payload, user2Socket.SentMessages[0].Data);
    }

    [Fact]
    public async Task User1SendsMessage_IsNotEchoedBack()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("hello"));
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Empty(user1Socket.SentMessages);
    }

    [Fact]
    public async Task User1SendsMessage_SavedToDatabase()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        var payload = Encoding.UTF8.GetBytes("persist me");
        user1Socket.EnqueueIncoming(payload);
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Single(db.SavedMessages);
        Assert.Equal(payload, db.SavedMessages[0].Payload);
    }

    [Fact]
    public async Task User1SendsMessage_SenderMarkedAsRead()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("hi"));
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Contains(db.MarkedAsRead, m => m.UserId == User1 && m.ChatId == ChatId);
    }

    [Fact]
    public async Task User1SendsMessage_RecipientMarkedAsRead()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("hi"));
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Contains(db.MarkedAsRead, m => m.UserId == User2 && m.ChatId == ChatId);
    }

    [Fact]
    public async Task MultipleUsers_AllReceiveMessage()
    {
        var user3Socket = new FakeWebSocket();
        var (user1Socket, user2Socket, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2, User3 });

        sockets.Add(user3Socket);
        userSockets[$"{User3}:{ChatId}"] = user3Socket;

        var payload = Encoding.UTF8.GetBytes("broadcast");
        user1Socket.EnqueueIncoming(payload);
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Single(user2Socket.SentMessages);
        Assert.Single(user3Socket.SentMessages);
        Assert.Equal(payload, user2Socket.SentMessages[0].Data);
        Assert.Equal(payload, user3Socket.SentMessages[0].Data);
    }

    [Fact]
    public async Task User1SendsMessage_NotificationSentToUser2()
    {
        var user2NotifSocket = new FakeWebSocket();
        var unread = new Dictionary<string, int> { [ChatId] = 1 };
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 },
            unreadCounts: unread);

        notifSockets[User2] = user2NotifSocket;

        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("ping"));
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Single(user2NotifSocket.SentMessages);
        var json = JsonDocument.Parse(user2NotifSocket.LastSentText()).RootElement;
        Assert.Equal("unread_counts", json.GetProperty("type").GetString());
    }

    [Fact]
    public async Task OnDisconnect_SocketRemovedFromLobbyAndUserSockets()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        lock (sockets) Assert.DoesNotContain(user1Socket, sockets);
        Assert.False(userSockets.ContainsKey($"{User1}:{ChatId}"));
    }

    [Fact]
    public async Task User2ConnectsAfterMessage_ReceivesItAsUnread()
    {
        // Arrange - User1 sends a message while User2 is offline
        var unread = new Dictionary<string, int> { [ChatId] = 1 };
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 },
            unreadCounts: unread);

        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("hello, anyone there?"));
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        // User2 now connects to the notification socket
        var user2NotifSocket = new FakeWebSocket();
        notifSockets[User2] = user2NotifSocket;

        // Act - simulate User2's notification connect (sends initial unread counts)
        await NotificationHelper.SendUnreadCounts(user2NotifSocket, db.GetUnreadCountsForUser(User2));

        // Assert - User2 should see 1 unread message in this chat
        Assert.Single(user2NotifSocket.SentMessages);
        var json = JsonDocument.Parse(user2NotifSocket.LastSentText()).RootElement;
        Assert.Equal("unread_counts", json.GetProperty("type").GetString());

        var data = json.GetProperty("data");
        Assert.Equal(1, data.GetProperty(ChatId).GetInt32());
    }

    // ── Unhappy flows ────────────────────────────────────────────────────────

    [Fact]
    public async Task UserNotAllowed_LoopExitsImmediately()
    {
        var socket = new FakeWebSocket();
        socket.EnqueueIncoming(Encoding.UTF8.GetBytes("should not be processed"));

        var sockets = new List<WebSocket> { socket };
        var userSockets = new ConcurrentDictionary<string, WebSocket>();
        userSockets[$"{User1}:{ChatId}"] = socket;

        // User1 is NOT in the allowed list
        var db = new FakeDatabaseConnection(new List<string> { User2 });

        await HandleAsync(socket, sockets, userSockets,
            new ConcurrentDictionary<string, WebSocket>(), db);

        Assert.Empty(db.SavedMessages);
    }

    [Fact]
    public async Task User2SocketClosed_OnlyUser3Receives()
    {
        var user3Socket = new FakeWebSocket();
        var (user1Socket, user2Socket, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2, User3 });

        sockets.Add(user3Socket);
        userSockets[$"{User3}:{ChatId}"] = user3Socket;

        // Close user2's socket before the message arrives
        await user2Socket.CloseAsync(WebSocketCloseStatus.NormalClosure, "", default);

        var payload = Encoding.UTF8.GetBytes("only user3 should get this");
        user1Socket.EnqueueIncoming(payload);
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Empty(user2Socket.SentMessages);
        Assert.Single(user3Socket.SentMessages);
    }

    [Fact]
    public async Task NoOtherUsersConnected_MessageSavedButNoBroadcast()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1 });

        // Only user1 in the lobby
        sockets.RemoveAll(s => s != user1Socket);

        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("alone"));
        user1Socket.EnqueueClose();

        await HandleAsync(user1Socket, sockets, userSockets, notifSockets, db);

        Assert.Single(db.SavedMessages);
    }

    [Fact]
    public async Task NotificationSocket_NotConnected_NoBroadcastError()
    {
        var (user1Socket, _, sockets, userSockets, notifSockets, db) = BuildChat(
            new List<string> { User1, User2 });

        // User2 has no notification socket connected
        user1Socket.EnqueueIncoming(Encoding.UTF8.GetBytes("hi"));
        user1Socket.EnqueueClose();

        // Should not throw
        var ex = await Record.ExceptionAsync(() =>
            HandleAsync(user1Socket, sockets, userSockets, notifSockets, db));

        Assert.Null(ex);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private (
        FakeWebSocket user1Socket,
        FakeWebSocket user2Socket,
        List<WebSocket> sockets,
        ConcurrentDictionary<string, WebSocket> userSockets,
        ConcurrentDictionary<string, WebSocket> notifSockets,
        FakeDatabaseConnection db)
    BuildChat(List<string> allowedUsers, Dictionary<string, int>? unreadCounts = null)
    {
        var user1Socket = new FakeWebSocket();
        var user2Socket = new FakeWebSocket();

        var sockets = new List<WebSocket> { user1Socket, user2Socket };

        var userSockets = new ConcurrentDictionary<string, WebSocket>();
        userSockets[$"{User1}:{ChatId}"] = user1Socket;
        userSockets[$"{User2}:{ChatId}"] = user2Socket;

        var notifSockets = new ConcurrentDictionary<string, WebSocket>();
        var db = new FakeDatabaseConnection(allowedUsers, unreadCounts);

        return (user1Socket, user2Socket, sockets, userSockets, notifSockets, db);
    }

    private async Task HandleAsync(
        FakeWebSocket webSocket,
        List<WebSocket> sockets,
        ConcurrentDictionary<string, WebSocket> userSockets,
        ConcurrentDictionary<string, WebSocket> notificationSockets,
        FakeDatabaseConnection db)
    {
        await ChatWebSocketHandler.HandleAsync(
            webSocket, sockets, ChatId, User1, db, notificationSockets, userSockets);
    }
}