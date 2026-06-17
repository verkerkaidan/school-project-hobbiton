using System.Net.WebSockets;
using System.Text;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using System.Linq;

/// <summary>
/// A controllable fake WebSocket that captures sent messages and lets you
/// feed incoming messages via EnqueueIncoming().
/// </summary>
public class FakeWebSocket : WebSocket
{
    private WebSocketState _state = WebSocketState.Open;
    private readonly Queue<byte[]> _incoming = new();
    public readonly List<(byte[] Data, WebSocketMessageType Type)> SentMessages = new();

    public override WebSocketState State => _state;
    public override string? SubProtocol => null;
    public override WebSocketCloseStatus? CloseStatus => null;
    public override string? CloseStatusDescription => null;

    /// <summary>Enqueue raw bytes to be returned by the next ReceiveAsync call.</summary>
    public void EnqueueIncoming(byte[] data) => _incoming.Enqueue(data);

    /// <summary>Enqueue a UTF-8 string as an incoming text message.</summary>
    public void EnqueueIncoming(string text) => EnqueueIncoming(Encoding.UTF8.GetBytes(text));

    /// <summary>Enqueue a Close frame as the next incoming message.</summary>
    public void EnqueueClose() => _incoming.Enqueue(Array.Empty<byte>());

    public override Task SendAsync(
        ArraySegment<byte> buffer,
        WebSocketMessageType messageType,
        bool endOfMessage,
        CancellationToken cancellationToken)
    {
        SentMessages.Add((buffer.ToArray(), messageType));
        return Task.CompletedTask;
    }

    public override Task<WebSocketReceiveResult> ReceiveAsync(
        ArraySegment<byte> buffer,
        CancellationToken cancellationToken)
    {
        if (_incoming.Count == 0 || _incoming.Peek().Length == 0)
        {
            _incoming.TryDequeue(out _);
            _state = WebSocketState.CloseReceived;
            return Task.FromResult(
                new WebSocketReceiveResult(0, WebSocketMessageType.Close, true));
        }

        var data = _incoming.Dequeue();
        data.CopyTo(buffer.Array!, buffer.Offset);

        return Task.FromResult(
            new WebSocketReceiveResult(data.Length, WebSocketMessageType.Binary, true));
    }

    public override Task CloseAsync(
        WebSocketCloseStatus closeStatus,
        string? statusDescription,
        CancellationToken cancellationToken)
    {
        _state = WebSocketState.Closed;
        return Task.CompletedTask;
    }

    public override Task CloseOutputAsync(
        WebSocketCloseStatus closeStatus,
        string? statusDescription,
        CancellationToken cancellationToken)
    {
        _state = WebSocketState.CloseSent;
        return Task.CompletedTask;
    }

    public override void Abort() => _state = WebSocketState.Aborted;
    public override void Dispose() { }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /// <summary>Returns the last sent message decoded as UTF-8 text.</summary>
    public string LastSentText()
    {
        var last = SentMessages.Last();
        return Encoding.UTF8.GetString(last.Data);
    }

    /// <summary>Returns all sent messages decoded as UTF-8 text.</summary>
    public IEnumerable<string> AllSentTexts() =>
        SentMessages.Select(m => Encoding.UTF8.GetString(m.Data));
}