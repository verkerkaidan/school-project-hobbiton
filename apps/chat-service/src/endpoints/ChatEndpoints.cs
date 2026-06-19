using System.Collections.Concurrent;
using System.Net.WebSockets;

public static class ChatEndpoints
{
    public static void Map(
        WebApplication app,
        ConcurrentDictionary<string, WebSocket> userSockets,
        DatabaseConnection db)
    {
        app.MapPost("/create-chat", (ChatModel chatModel) =>
        {
            var chatId = Guid.NewGuid();
            db.createChat(chatId, chatModel.title);
            db.AllowUsers(chatId.ToString(), chatModel.userIds);
            return Results.Ok(chatId);
        });

        app.MapGet("/get-chats/{userId}", (string userId) =>
            Results.Ok(db.getAllChatsFromUser(userId)));

        app.MapGet("/get-chat-title/{chatId}", (string chatId) =>
            Results.Ok(db.getTitleFromChatId(chatId)));

        app.MapPut("/update-title", (TitleModel titleModel) =>
            db.alterTitleFromChatId(titleModel));

        app.MapPut("/add-user-to-chat", (AlterUserModel alterUserModel) =>
            Results.Ok(db.addUserToChat(alterUserModel)));

        app.MapPut("/delete-user-from-chat", async (AlterUserModel alterUserModel) =>
        {
            bool wasRemoved = db.removeUserFromChat(alterUserModel);
            if (wasRemoved)
            {
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

        app.MapGet("/allowed-users/{chatId}", (string chatId) => db.chatUserList(chatId));

        app.MapDelete("/delete-user", async (Guid deleteUserId) =>
        {
            bool wasRemoved = db.RemoveAllUserInformation(deleteUserId);
            return Results.Ok(wasRemoved);
        });
    }
}