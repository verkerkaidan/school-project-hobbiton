using System.Collections.Concurrent;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using System.Collections.Generic;
using Xunit;

public class NotificationWebSocketHandlerTests
{
    private static readonly string ChatId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    private static readonly string UserId = "3fa85f64-5717-4562-b3fc-2c963f66afa1";

    // ── Happy flows ──────────────────────────────────────────────────────────

    [Fact]
    public async Task OnConnect_SendsInitialUnreadCounts()
    {
        // Arrange
        var unread = new Dictionary<string, int> { [ChatId] = 3 };
        var db = new FakeDatabaseConnection(new List<string> { UserId }, unread);
        var socket = new FakeWebSocket();
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.Single(socket.SentMessages);
        var json = JsonDocument.Parse(socket.LastSentText()).RootElement;
        Assert.Equal("unread_counts", json.GetProperty("type").GetString());
        Assert.Equal(3, json.GetProperty("data").GetProperty(ChatId).GetInt32());
    }

    [Fact]
    public async Task MarkChatAsRead_CallsDbAndSendsUpdatedCounts()
    {
        // Arrange
        var unread = new Dictionary<string, int> { [ChatId] = 2 };
        var db = new FakeDatabaseConnection(new List<string> { UserId }, unread);
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming(BuildJson(new { type = "mark_chat_as_read", chatId = ChatId }));
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.Contains(db.MarkedAsRead, m => m.UserId == UserId && m.ChatId == ChatId);

        // Two sends: initial counts + updated counts after marking read
        Assert.Equal(2, socket.SentMessages.Count);
        Assert.All(socket.AllSentTexts(), text =>
        {
            var json = JsonDocument.Parse(text).RootElement;
            Assert.Equal("unread_counts", json.GetProperty("type").GetString());
        });
    }

    [Fact]
    public async Task MarkAllAsRead_CallsDbAndSendsUpdatedCounts()
    {
        // Arrange
        var unread = new Dictionary<string, int> { [ChatId] = 5 };
        var db = new FakeDatabaseConnection(new List<string> { UserId }, unread);
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming(BuildJson(new { type = "mark_all_as_read" }));
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.True(db.MarkAllCalled);
        Assert.Equal(2, socket.SentMessages.Count);
    }

    [Fact]
    public async Task OnConnect_NoUnreadMessages_SendsEmptyCounts()
    {
        // Arrange
        var db = new FakeDatabaseConnection(new List<string> { UserId });
        var socket = new FakeWebSocket();
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.Single(socket.SentMessages);
        var json = JsonDocument.Parse(socket.LastSentText()).RootElement;
        Assert.Equal("unread_counts", json.GetProperty("type").GetString());
    }

    [Fact]
    public async Task MultipleMessages_EachUpdatesUnreadCounts()
    {
        // Arrange
        var db = new FakeDatabaseConnection(new List<string> { UserId });
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming(BuildJson(new { type = "mark_chat_as_read", chatId = ChatId }));
        socket.EnqueueIncoming(BuildJson(new { type = "mark_all_as_read" }));
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        // Initial + after mark_chat_as_read + after mark_all_as_read = 3 sends
        Assert.Equal(3, socket.SentMessages.Count);
        Assert.Contains(db.MarkedAsRead, m => m.UserId == UserId && m.ChatId == ChatId);
        Assert.True(db.MarkAllCalled);
    }

    // ── Unhappy flows ────────────────────────────────────────────────────────

    [Fact]
    public async Task MarkChatAsRead_MissingChatId_DoesNotCallDb()
    {
        // Arrange
        var db = new FakeDatabaseConnection(new List<string> { UserId });
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming(BuildJson(new { type = "mark_chat_as_read" })); // no chatId
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.Empty(db.MarkedAsRead);
    }

    [Fact]
    public async Task UnknownMessageType_DoesNotCallAnyDbWrite()
    {
        // Arrange
        var db = new FakeDatabaseConnection(new List<string> { UserId });
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming(BuildJson(new { type = "unknown_type" }));
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.Empty(db.MarkedAsRead);
        Assert.False(db.MarkAllCalled);
        Assert.Single(socket.SentMessages); // only initial counts
    }

    [Fact]
    public async Task MalformedJson_DoesNotThrow_AndContinuesLoop()
    {
        // Arrange
        var db = new FakeDatabaseConnection(new List<string> { UserId });
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming("this is not json {{{");
        socket.EnqueueClose();

        // Act — handler catches parse errors internally, should not throw
        var ex = await Record.ExceptionAsync(() =>
            NotificationWebSocketHandler.HandleAsync(socket, UserId, db));

        Assert.Null(ex);
    }

    [Fact]
    public async Task MalformedJson_StillSendsInitialCounts()
    {
        // Arrange
        var unread = new Dictionary<string, int> { [ChatId] = 1 };
        var db = new FakeDatabaseConnection(new List<string> { UserId }, unread);
        var socket = new FakeWebSocket();

        socket.EnqueueIncoming("not json at all");
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert — initial send still happened despite bad message
        Assert.Single(socket.SentMessages);
        var json = JsonDocument.Parse(socket.LastSentText()).RootElement;
        Assert.Equal("unread_counts", json.GetProperty("type").GetString());
    }

    [Fact]
    public async Task CloseFrame_ClosesGracefully()
    {
        // Arrange
        var db = new FakeDatabaseConnection(new List<string> { UserId });
        var socket = new FakeWebSocket();
        socket.EnqueueClose();

        // Act
        await NotificationWebSocketHandler.HandleAsync(socket, UserId, db);

        // Assert
        Assert.Equal(WebSocketState.Closed, socket.State);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static byte[] BuildJson(object payload) =>
        Encoding.UTF8.GetBytes(JsonSerializer.Serialize(payload));
}