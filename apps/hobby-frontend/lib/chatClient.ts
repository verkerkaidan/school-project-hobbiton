// const API_URL = "http://localhost:29294";
const API_URL = process.env.NEXT_PUBLIC_CHAT_SERVICE_URL;

export async function getChats(userId: string) {
  console.log('hello world');
  const res = await fetch(
    `${API_URL}/get-chats/${userId}`,
    {
      cache: "no-store",
    }
  );

  return res.json();
}

export async function createChat(userIds: string[]) {
  const res = await fetch(
    `${API_URL}/create-chat`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userIds),
    }
  );

  return res.json();
}