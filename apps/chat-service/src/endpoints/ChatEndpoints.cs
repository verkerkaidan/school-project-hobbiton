using System.Collections.Concurrent;
using System.Net.WebSockets;

public static class ChatEndpoints
{
    public static void Map(
        WebApplication app,
        ConcurrentDictionary<string, WebSocket> userSockets,
        IDatabaseConnection db)
    {
        app.MapPost("/create-chat", (ChatModel chatModel) =>
        {
            var chatId = Guid.NewGuid();
            AppLogger.Info($"Creating chat '{chatModel.title}' with id {chatId} for {chatModel.userIds.Count} users");
            db.createChat(chatId, chatModel.title);
            db.AllowUsers(chatId.ToString(), chatModel.userIds);
            AppLogger.Info($"Chat {chatId} created successfully");
            return Results.Ok(chatId);
        });

        app.MapGet("/get-chats/{userId}", (string userId) =>
        {
            AppLogger.Info($"Fetching chats for user {userId}");
            return Results.Ok(db.getAllChatsFromUser(userId));
        });

        app.MapGet("/get-chat-title/{chatId}", (string chatId) =>
        {
            AppLogger.Info($"Fetching title for chat {chatId}");
            return Results.Ok(db.getTitleFromChatId(chatId));
        });

        app.MapPut("/update-title", (TitleModel titleModel) =>
        {
            AppLogger.Info($"Updating title for chat {titleModel.chatId} to '{titleModel.title}'");
            return db.alterTitleFromChatId(titleModel);
        });

        app.MapPut("/add-user-to-chat", (AlterUserModel alterUserModel) =>
        {
            AppLogger.Info($"Adding user {alterUserModel.userId} to chat {alterUserModel.chatId}");
            return Results.Ok(db.addUserToChat(alterUserModel));
        });

        app.MapPut("/delete-user-from-chat", async (AlterUserModel alterUserModel) =>
        {
            AppLogger.Info($"Removing user {alterUserModel.userId} from chat {alterUserModel.chatId}");
            bool wasRemoved = db.removeUserFromChat(alterUserModel);
            if (wasRemoved)
            {
                var key = $"{alterUserModel.userId}:{alterUserModel.chatId}";
                if (userSockets.TryRemove(key, out var webSocket))
                {
                    AppLogger.Info($"Closing WebSocket for user {alterUserModel.userId} in chat {alterUserModel.chatId}");
                    await webSocket.CloseAsync(
                        WebSocketCloseStatus.NormalClosure,
                        "Removed from chat",
                        CancellationToken.None);
                }
            }
            else
            {
                AppLogger.Warn($"User {alterUserModel.userId} could not be removed from chat {alterUserModel.chatId}");
            }
            return Results.Ok(wasRemoved);
        });

        app.MapGet("/allowed-users/{chatId}", (string chatId) =>
        {
            AppLogger.Info($"Fetching allowed users for chat {chatId}");
            return db.chatUserList(chatId);
        });

        app.MapDelete("/delete-user", async (Guid deleteUserId) =>
        {
            AppLogger.Info($"Deleting all data for user {deleteUserId}");
            bool wasRemoved = db.RemoveAllUserInformation(deleteUserId);
            if (!wasRemoved) AppLogger.Warn($"No data found to delete for user {deleteUserId}");
            return Results.Ok(wasRemoved);
        });
    }
}