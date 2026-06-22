using System;
using System.Collections.Generic;

public class FakeDatabaseConnection : IDatabaseConnection
{
    private readonly List<string> _allowedUsers;
    private readonly Dictionary<string, int> _unreadCounts;

    public List<(string ChatId, byte[] Payload)> SavedMessages = new();
    public List<(string UserId, string ChatId)> MarkedAsRead = new();
    public bool MarkAllCalled = false;

    // Tracking for endpoint operations
    public List<(Guid ChatId, string Title)> CreatedChats = new();
    public List<(string ChatId, List<string> UserIds)> GrantedAccess = new();
    public List<string> GetChatsCalls = new();
    public List<string> GetTitleCalls = new();
    public List<TitleModel> AlteredTitles = new();
    public List<AlterUserModel> AddedUsers = new();
    public List<AlterUserModel> RemovedUsers = new();
    public List<Guid> RemovedAllUserData = new();
    public List<string> AllowedUserListCalls = new();

    // Configurable return values
    public List<object>? GetAllChatsResult { get; set; }
    public string? GetTitleResult { get; set; }
    public string? AlterTitleResult { get; set; }
    public bool AddUserResult { get; set; } = true;
    public bool RemoveUserResult { get; set; } = true;
    public bool RemoveAllUserResult { get; set; } = true;

    public FakeDatabaseConnection(
        List<string> allowedUsers,
        Dictionary<string, int>? unreadCounts = null)
    {
        _allowedUsers = allowedUsers;
        _unreadCounts = unreadCounts ?? new();
    }

    public bool isUserAllowed(string chatId, string userId) => _allowedUsers.Contains(userId);
    public List<string> chatUserList(string chatId) { AllowedUserListCalls.Add(chatId); return _allowedUsers; }
    public List<byte[]> GetChatHistory(string chatId) => new();
    public void SaveMessage(string chatId, byte[] payload) => SavedMessages.Add((chatId, payload));
    public void MarkChatAsRead(string userId, string chatId) => MarkedAsRead.Add((userId, chatId));
    public void MarkAllChatsAsRead(string userId) => MarkAllCalled = true;
    public Dictionary<string, int> GetUnreadCountsForUser(string userId) => _unreadCounts;
    public void AllowUsers(string chatId, List<string> userIdList) => GrantedAccess.Add((chatId, userIdList));
    public void createChat(Guid chatId, string title) => CreatedChats.Add((chatId, title));
    public List<object> getAllChatsFromUser(string userId) { GetChatsCalls.Add(userId); return GetAllChatsResult ?? new(); }
    public string getTitleFromChatId(string chatId) { GetTitleCalls.Add(chatId); return GetTitleResult ?? ""; }
    public string alterTitleFromChatId(TitleModel titleModel) { AlteredTitles.Add(titleModel); return AlterTitleResult ?? titleModel.title; }
    public bool removeUserFromChat(AlterUserModel alterUserModel) { RemovedUsers.Add(alterUserModel); return RemoveUserResult; }
    public bool addUserToChat(AlterUserModel alterUserModel) { AddedUsers.Add(alterUserModel); return AddUserResult; }
    public bool RemoveAllUserInformation(Guid userId) { RemovedAllUserData.Add(userId); return RemoveAllUserResult; }
}