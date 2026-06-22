public interface IDatabaseConnection
{
    bool isUserAllowed(string chatId, string userId);
    void AllowUsers(string chatId, List<string> userIdList);
    List<string> chatUserList(string chatId);
    List<byte[]> GetChatHistory(string chatId);
    void SaveMessage(string chatId, byte[] payload);
    void createChat(Guid chatId, string title);
    List<object> getAllChatsFromUser(string userId);
    string getTitleFromChatId(string chatId);
    string alterTitleFromChatId(TitleModel titleModel);
    bool removeUserFromChat(AlterUserModel alterUserModel);
    bool addUserToChat(AlterUserModel alterUserModel);
    Dictionary<string, int> GetUnreadCountsForUser(string userId);
    void MarkChatAsRead(string userId, string chatId);
    void MarkAllChatsAsRead(string userId);
    bool RemoveAllUserInformation(Guid userId);
}