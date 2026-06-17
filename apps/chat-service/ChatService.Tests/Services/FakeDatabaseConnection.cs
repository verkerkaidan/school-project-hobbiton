using System;
using System.Collections.Generic;

public class FakeDatabaseConnection : IDatabaseConnection
{
    private readonly List<string> _allowedUsers;
    private readonly Dictionary<string, int> _unreadCounts;

    public List<(string ChatId, byte[] Payload)> SavedMessages = new();
    public List<(string UserId, string ChatId)> MarkedAsRead = new();
    public bool MarkAllCalled = false;

    public FakeDatabaseConnection(
        List<string> allowedUsers,
        Dictionary<string, int>? unreadCounts = null)
    {
        _allowedUsers = allowedUsers;
        _unreadCounts = unreadCounts ?? new();
    }

    public bool isUserAllowed(string chatId, string userId) => _allowedUsers.Contains(userId);
    public List<string> chatUserList(string chatId) => _allowedUsers;
    public List<byte[]> GetChatHistory(string chatId) => new();
    public void SaveMessage(string chatId, byte[] payload) => SavedMessages.Add((chatId, payload));
    public void MarkChatAsRead(string userId, string chatId) => MarkedAsRead.Add((userId, chatId));
    public void MarkAllChatsAsRead(string userId) => MarkAllCalled = true;
    public Dictionary<string, int> GetUnreadCountsForUser(string userId) => _unreadCounts;

    // Unused in tests — empty implementations
    public void AllowUsers(string chatId, List<string> userIdList) { }
    public void createChat(Guid chatId, string title) { }
    public List<object> getAllChatsFromUser(string userId) => new();
    public string getTitleFromChatId(string chatId) => "";
    public string alterTitleFromChatId(TitleModel titleModel) => "";
    public bool removeUserFromChat(AlterUserModel alterUserModel) => false;
    public bool addUserToChat(AlterUserModel alterUserModel) => false;
}