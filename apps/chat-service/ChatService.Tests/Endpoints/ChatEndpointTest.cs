using System.Collections.Concurrent;
using System.Net;
using System.Net.Http.Json;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Logging;
using Xunit;

public class ChatEndpointTest
{
    private static readonly string ChatId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    private static readonly string User1 = "3fa85f64-5717-4562-b3fc-2c963f66afa1";
    private static readonly string User2 = "3fa85f64-5717-4562-b3fc-2c963f66afa2";

    // ── Helpers ──────────────────────────────────────────────────────────────

    private (WebApplication App, FakeDatabaseConnection Db, ConcurrentDictionary<string, WebSocket> UserSockets)
    BuildApp()
    {
        var db = new FakeDatabaseConnection(new List<string> { User1, User2 });
        var userSockets = new ConcurrentDictionary<string, WebSocket>();

        var builder = WebApplication.CreateBuilder();
        builder.Logging.ClearProviders();
        builder.WebHost.UseUrls("http://127.0.0.1:0");
        var app = builder.Build();
        ChatEndpoints.Map(app, userSockets, db);

        return (app, db, userSockets);
    }

    private async Task<HttpClient> StartApp(WebApplication app)
    {
        await app.StartAsync();
        var port = app.Urls.First().Split(':').Last();
        return new HttpClient { BaseAddress = new Uri($"http://127.0.0.1:{port}") };
    }

    private static StringContent JsonBody(object value) =>
        new(JsonSerializer.Serialize(value), Encoding.UTF8, "application/json");

    // ══════════════════════════════════════════════════════════════════════════
    // POST /create-chat
    // ══════════════════════════════════════════════════════════════════════════

    public class CreateChat : ChatEndpointTest
    {
        [Fact]
        public async Task Returns_201_With_ChatId()
        {
            var (app, db, _) = BuildApp();
            using var client = await StartApp(app);

            var body = new { userIds = new[] { User1, User2 }, title = "Test Chat" };
            var response = await client.PostAsync("/create-chat", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var chatId = await response.Content.ReadFromJsonAsync<string>();
            Assert.Matches(@"^[0-9a-f-]{36}$", chatId!);
        }

        [Fact]
        public async Task Calls_CreateChat_And_AllowUsers()
        {
            var (app, db, _) = BuildApp();
            using var client = await StartApp(app);

            var body = new { userIds = new[] { User1, User2 }, title = "Test Chat" };
            await client.PostAsync("/create-chat", JsonBody(body));

            Assert.Single(db.CreatedChats);
            Assert.Equal("Test Chat", db.CreatedChats[0].Title);

            Assert.Single(db.GrantedAccess);
            Assert.Equal(new[] { User1, User2 }, db.GrantedAccess[0].UserIds);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /get-chats/{userId}
    // ══════════════════════════════════════════════════════════════════════════

    public class GetChats : ChatEndpointTest
    {
        [Fact]
        public async Task Returns_Chats_For_User()
        {
            var (app, db, _) = BuildApp();
            db.GetAllChatsResult = new List<object> { new { chatId = ChatId, title = "Test" } };
            using var client = await StartApp(app);

            var response = await client.GetAsync($"/get-chats/{User1}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            Assert.Single(db.GetChatsCalls);
            Assert.Equal(User1, db.GetChatsCalls[0]);
        }

        [Fact]
        public async Task Returns_Empty_When_No_Chats()
        {
            var (app, db, _) = BuildApp();
            db.GetAllChatsResult = new List<object>();
            using var client = await StartApp(app);

            var response = await client.GetAsync($"/get-chats/{User1}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var chats = await response.Content.ReadFromJsonAsync<List<object>>();
            Assert.Empty(chats!);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /get-chat-title/{chatId}
    // ══════════════════════════════════════════════════════════════════════════

    public class GetChatTitle : ChatEndpointTest
    {
        [Fact]
        public async Task Returns_Title()
        {
            var (app, db, _) = BuildApp();
            db.GetTitleResult = "My Chat";
            using var client = await StartApp(app);

            var response = await client.GetAsync($"/get-chat-title/{ChatId}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var title = await response.Content.ReadFromJsonAsync<string>();
            Assert.Equal("My Chat", title);
            Assert.Single(db.GetTitleCalls);
            Assert.Equal(ChatId, db.GetTitleCalls[0]);
        }

        [Fact]
        public async Task Returns_Empty_When_Not_Found()
        {
            var (app, db, _) = BuildApp();
            db.GetTitleResult = "";
            using var client = await StartApp(app);

            var response = await client.GetAsync($"/get-chat-title/{ChatId}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var title = await response.Content.ReadFromJsonAsync<string>();
            Assert.Equal("", title);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PUT /update-title
    // ══════════════════════════════════════════════════════════════════════════

    public class UpdateTitle : ChatEndpointTest
    {
        [Fact]
        public async Task Renames_Chat()
        {
            var (app, db, _) = BuildApp();
            db.AlterTitleResult = "New Name";
            using var client = await StartApp(app);

            var body = new { chatId = ChatId, title = "New Name" };
            var response = await client.PutAsync("/update-title", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadAsStringAsync();
            Assert.Equal("New Name", result);

            Assert.Single(db.AlteredTitles);
            Assert.Equal(ChatId, db.AlteredTitles[0].chatId);
            Assert.Equal("New Name", db.AlteredTitles[0].title);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PUT /add-user-to-chat
    // ══════════════════════════════════════════════════════════════════════════

    public class AddUserToChat : ChatEndpointTest
    {
        [Fact]
        public async Task Adds_User_And_Returns_True()
        {
            var (app, db, _) = BuildApp();
            db.AddUserResult = true;
            using var client = await StartApp(app);

            var body = new { userId = User2, chatId = ChatId };
            var response = await client.PutAsync("/add-user-to-chat", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.True(result);

            Assert.Single(db.AddedUsers);
            Assert.Equal(User2, db.AddedUsers[0].userId);
            Assert.Equal(ChatId, db.AddedUsers[0].chatId);
        }

        [Fact]
        public async Task Returns_False_When_User_Already_In_Chat()
        {
            var (app, db, _) = BuildApp();
            db.AddUserResult = false;
            using var client = await StartApp(app);

            var body = new { userId = User2, chatId = ChatId };
            var response = await client.PutAsync("/add-user-to-chat", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.False(result);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PUT /delete-user-from-chat
    // ══════════════════════════════════════════════════════════════════════════

    public class DeleteUserFromChat : ChatEndpointTest
    {
        [Fact]
        public async Task Removes_User_And_Returns_True()
        {
            var (app, db, userSockets) = BuildApp();
            var fakeSocket = new FakeWebSocket();
            userSockets[$"{User2}:{ChatId}"] = fakeSocket;
            db.RemoveUserResult = true;
            using var client = await StartApp(app);

            var body = new { userId = User2, chatId = ChatId };
            var response = await client.PutAsync("/delete-user-from-chat", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.True(result);

            Assert.Single(db.RemovedUsers);
            Assert.Equal(User2, db.RemovedUsers[0].userId);
            Assert.Equal(ChatId, db.RemovedUsers[0].chatId);
        }

        [Fact]
        public async Task Closes_User_WebSocket_On_Removal()
        {
            var (app, db, userSockets) = BuildApp();
            var fakeSocket = new FakeWebSocket();
            userSockets[$"{User2}:{ChatId}"] = fakeSocket;
            db.RemoveUserResult = true;
            using var client = await StartApp(app);

            var body = new { userId = User2, chatId = ChatId };
            await client.PutAsync("/delete-user-from-chat", JsonBody(body));

            Assert.Equal(WebSocketState.Closed, fakeSocket.State);
        }

        [Fact]
        public async Task Returns_False_When_User_Not_In_Chat()
        {
            var (app, db, _) = BuildApp();
            db.RemoveUserResult = false;
            using var client = await StartApp(app);

            var body = new { userId = User2, chatId = ChatId };
            var response = await client.PutAsync("/delete-user-from-chat", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.False(result);
        }

        [Fact]
        public async Task Does_Not_Throw_When_User_Has_No_Socket()
        {
            var (app, db, _) = BuildApp();
            db.RemoveUserResult = true;
            using var client = await StartApp(app);

            var body = new { userId = User2, chatId = ChatId };
            var response = await client.PutAsync("/delete-user-from-chat", JsonBody(body));

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.True(result);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /allowed-users/{chatId}
    // ══════════════════════════════════════════════════════════════════════════

    public class GetAllowedUsers : ChatEndpointTest
    {
        [Fact]
        public async Task Returns_User_List()
        {
            var (app, db, _) = BuildApp();
            using var client = await StartApp(app);

            var response = await client.GetAsync($"/allowed-users/{ChatId}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var users = await response.Content.ReadFromJsonAsync<List<string>>();
            Assert.Equal(new[] { User1, User2 }, users);
            Assert.Single(db.AllowedUserListCalls);
            Assert.Equal(ChatId, db.AllowedUserListCalls[0]);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DELETE /delete-user
    // ══════════════════════════════════════════════════════════════════════════

    public class DeleteUser : ChatEndpointTest
    {
        [Fact]
        public async Task Removes_User_Data_And_Returns_True()
        {
            var (app, db, _) = BuildApp();
            db.RemoveAllUserResult = true;
            var userId = Guid.NewGuid();
            using var client = await StartApp(app);

            var response = await client.DeleteAsync($"/delete-user?deleteUserId={userId}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.True(result);

            Assert.Single(db.RemovedAllUserData);
            Assert.Equal(userId, db.RemovedAllUserData[0]);
        }

        [Fact]
        public async Task Returns_False_When_No_Data()
        {
            var (app, db, _) = BuildApp();
            db.RemoveAllUserResult = false;
            using var client = await StartApp(app);

            var response = await client.DeleteAsync($"/delete-user?deleteUserId={Guid.NewGuid()}");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            var result = await response.Content.ReadFromJsonAsync<bool>();
            Assert.False(result);
        }
    }
}
