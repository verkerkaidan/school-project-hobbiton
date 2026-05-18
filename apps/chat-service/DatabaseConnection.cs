using Microsoft.VisualBasic;
using Npgsql;

class DatabaseConnection
{
    private NpgsqlConnection CreateConnection()
    {
        var conn = new NpgsqlConnection(
            "Host=database;Port=5432;Username=chat_database_user;Password=KDImmI!k;Database=chat_service"
        );

        conn.Open();
        return conn;
    }

    public bool isUserAllowed(string chatId, string userId)
    {
        const string sql = """
    SELECT EXISTS (
        SELECT 1 FROM allowed_users
        WHERE chat_id = @chatId AND user_id = @userId
    )
    """;

        using var conn = CreateConnection();
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

        using var conn = CreateConnection();
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

    public List<byte[]> GetChatHistory(string chatId)
    {
        const string sql = """
    SELECT protobuf FROM chat_item
    WHERE chat_id = @chatId
    """;

        using var conn = CreateConnection();
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

        using var conn = CreateConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", Guid.NewGuid());
        cmd.Parameters.AddWithValue("@chatId", Guid.Parse(chatId));
        cmd.Parameters.AddWithValue("@protobuf", payload);

        cmd.ExecuteNonQuery();
    }
    public void createChat(Guid chatId)
    {
        const string sql = """
    INSERT INTO chat (chat_id)
    VALUES (@chatId)
    """;

        using var conn = CreateConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@chatId", chatId);

        cmd.ExecuteNonQuery();
    }

    public List<string> getAllChatsFromUser(string userId)
    {
        const string sql = """
    SELECT chat_id FROM allowed_users
    WHERE user_id = @userId
    """;

        using var conn = CreateConnection();
        using var cmd = new NpgsqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@userId", Guid.Parse(userId));

        var chatIds = new List<string>();

        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            chatIds.Add(reader["chat_id"].ToString()!);

        return chatIds;
    }
}