using Microsoft.VisualBasic;
using Npgsql;

public class DatabaseConnection
{
    private readonly NpgsqlDataSource _dataSource;

    public DatabaseConnection()
    {
        var connectionString = Environment.GetEnvironmentVariable("CHAT_DB_CONNECTION_STRING")
            ?? throw new InvalidOperationException("CHAT_DB_CONNECTION_STRING is not set");
        _dataSource = NpgsqlDataSource.Create(connectionString);
    }

    public bool isUserAllowed(string chatId, string userId)
    {
        const string sql = """
    SELECT EXISTS (
        SELECT 1 FROM allowed_users
        WHERE chat_id = @chatId AND user_id = @userId
    )
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));
        cmd.Parameters.AddWithValue("@userId", Guid.Parse(userId));

        return (bool)cmd.ExecuteScalar()!;
    }

    public void AllowUsers(string chatId, List<string> userIdList)
    {
        const string sql = """
    INSERT INTO allowed_users (allowed_user_id, chat_id, user_id)
    VALUES (@id, @chatId, @userId)
    ON CONFLICT (chat_id, user_id) DO NOTHING
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.Add("@id", NpgsqlTypes.NpgsqlDbType.Uuid);
        cmd.Parameters.Add("@chatId", NpgsqlTypes.NpgsqlDbType.Uuid);
        cmd.Parameters.Add("@userId", NpgsqlTypes.NpgsqlDbType.Uuid);

        foreach (var userId in userIdList)
        {
            cmd.Parameters["@id"].Value = Guid.NewGuid();
            cmd.Parameters["@chatId"].Value = Guid.Parse(chatId);
            cmd.Parameters["@userId"].Value = Guid.Parse(userId);

            cmd.ExecuteNonQuery();
        }
    }

    public List<String> chatUserList(string chatId)
    {
        const string sql = """
        SELECT user_id
        FROM allowed_users
        WHERE chat_id = @chatId
        """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));

        var allowedUsers = new List<string>();
        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            allowedUsers.Add(reader["user_id"].ToString()!);

        return allowedUsers;
    }
    public List<byte[]> GetChatHistory(string chatId)
    {
        const string sql = """
    SELECT protobuf FROM chat_item
    WHERE chat_id = @chatId
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));

        var history = new List<byte[]>();

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            history.Add((byte[])reader["protobuf"]);

        return history;
    }

    public void SaveMessage(string chatId, byte[] payload)
    {
        const string sql = """
    INSERT INTO chat_item (chat_item_id, chat_id, protobuf)
    VALUES (@id, @chatId, @protobuf)
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", Guid.NewGuid());
        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));
        cmd.Parameters.AddWithValue("@protobuf", payload);

        cmd.ExecuteNonQuery();
    }
    public void createChat(Guid chatId, string title)
    {
        const string sql = """
    INSERT INTO chat (chat_id, title)
    VALUES (@chatId, @title)
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@chatId", chatId);
        cmd.Parameters.AddWithValue("@title", title);

        cmd.ExecuteNonQuery();
    }

    public List<object> getAllChatsFromUser(string userId)
    {
        const string sql = """
    SELECT au.chat_id,
           c.title,
           MAX(ci.created_at) AS latest_updated
    FROM allowed_users au
    LEFT JOIN chat c ON c.chat_id = au.chat_id
    LEFT JOIN chat_item ci ON ci.chat_id = au.chat_id
    WHERE au.user_id = @userId
    GROUP BY au.chat_id, c.title
    ORDER BY MAX(ci.created_at) DESC NULLS LAST
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@userId", Guid.Parse(userId));

        var chats = new List<object>();

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            chats.Add(new
            {
                chatId = reader["chat_id"].ToString()!,
                title = reader["title"].ToString()!
            });

        return chats;
    }
    public string getTitleFromChatId(string chatId)
    {
        const string sql = """
    SELECT title 
    FROM chat
    WHERE chat_id = @chatId
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            return reader["title"].ToString()!;

        return "";
    }

    public string alterTitleFromChatId(TitleModel titleModel)
    {
        const string sql = """
    UPDATE chat
    SET title = @title
    WHERE chat_id = @chatId
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@title", titleModel.title);
        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(titleModel.chatId));

        cmd.ExecuteNonQuery();
        return titleModel.title;
    }

    public bool removeUserFromChat(AlterUserModel alterUserModel)
    {
        const string sql = """
        DELETE FROM allowed_users
        WHERE user_id = @userId AND chat_id = @chatId
        """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@userId", Guid.Parse(alterUserModel.userId));
        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(alterUserModel.chatId));

        var rowsAffected = cmd.ExecuteNonQuery();
        return rowsAffected > 0;
    }

    public bool addUserToChat(AlterUserModel alterUserModel)
    {
        const string sql = """
        INSERT INTO allowed_users (allowed_user_id, chat_id, user_id)
        VALUES (@id, @chatId, @userId)
        ON CONFLICT (chat_id, user_id) DO NOTHING
        """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", Guid.NewGuid());
        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(alterUserModel.chatId));
        cmd.Parameters.AddWithValue("@userId", Guid.Parse(alterUserModel.userId));

        var rowsAffected = cmd.ExecuteNonQuery();
        return rowsAffected > 0;
    }

    public Dictionary<string, int> GetUnreadCountsForUser(string userId)
    {
        const string sql = """
        SELECT ci.chat_id, COUNT(*) as unread
        FROM chat_item ci
        LEFT JOIN user_chat_state ucs ON ucs.chat_id = ci.chat_id AND ucs.user_id = @userId
        WHERE ci.chat_id IN (SELECT chat_id FROM allowed_users WHERE user_id = @userId)
        AND (ucs.last_read_at IS NULL OR ci.created_at > ucs.last_read_at)
        GROUP BY ci.chat_id
        """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@userId", Guid.Parse(userId));

        var unreadCounts = new Dictionary<string, int>();

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            unreadCounts.Add(
                reader["chat_id"].ToString()!,
                Convert.ToInt32(reader["unread"])
            );

        return unreadCounts;
    }

    public void MarkChatAsRead(string userId, string chatId)
    {
        Console.WriteLine($"Marking chat {chatId} as read for user {userId}");
        const string sql = """
    INSERT INTO user_chat_state (user_id, chat_id, last_read_at)
    VALUES (@userId, @chatId, NOW())
    ON CONFLICT (user_id, chat_id) DO UPDATE
    SET last_read_at = NOW()
    """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@userId", Guid.Parse(userId));
        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));

        cmd.ExecuteNonQuery();
    }

    public void MarkAllChatsAsRead(string userId)
    {
        const string sql = """
        INSERT INTO user_chat_state (user_id, chat_id, last_read_at)
        SELECT @userId, allowed_users.chat_id, NOW()
        FROM allowed_users
        WHERE allowed_users.user_id = @userId
        ON CONFLICT (user_id, chat_id) DO UPDATE
        SET last_read_at = NOW()
        """;

        using var conn = _dataSource.OpenConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", Guid.NewGuid());
        cmd.Parameters.AddWithValue("@userId", Guid.Parse(userId));

        cmd.ExecuteNonQuery();
    }
}