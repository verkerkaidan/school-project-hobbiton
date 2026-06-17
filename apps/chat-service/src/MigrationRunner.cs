
using Npgsql;

class MigrationRunner
{
    private readonly string _connectionString;

    public MigrationRunner(string connectionString)
    {
        _connectionString = connectionString;
    }

    public void Run()
    {
        var migrationFiles = Directory.GetFiles("migrations", "*.sql")
            .OrderBy(f => f); // runs in filename order: 001_, 002_, etc.

        using var conn = new NpgsqlConnection(_connectionString);
        conn.Open();

        foreach (var file in migrationFiles)
        {
            var sql = File.ReadAllText(file);
            using var cmd = new NpgsqlCommand(sql, conn);
            cmd.ExecuteNonQuery();
            Console.WriteLine($"Ran migration: {file}");
        }
    }
}