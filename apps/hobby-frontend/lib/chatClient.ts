import { getProfile } from "@/queries/getProfile";
import { User } from "@/types/profile";

// const API_URL = "http://localhost:29294";
const API_URL = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE + "/api";

export async function getChats(userId: string) {
  const res = await fetch(`${API_URL}/get-chats/${userId}`, {
    cache: "no-store",
  });

  return res.json();
}

export async function createChat(object: object) {
  const res = await fetch(`${API_URL}/create-chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(object),
  });

  return res.json();
}

export async function getChatTitle(chatId: string): Promise<string> {
  const res = await fetch(`${API_URL}/get-chat-title/${chatId}`, {
    cache: "no-store",
  });

  return res.json();
}

export async function getAllowedUsersFromChat(
  chatId: string,
): Promise<string[]> {
  const res = await fetch(`${API_URL}/allowed-users/${chatId}`, {
    cache: "no-store",
  });

  return res.json();
}

export async function fetchUsersFromChat(chatId: string): Promise<User[]> {
  const usersList: User[] = [];
  const allowedUserIds = await getAllowedUsersFromChat(chatId);

  for (const userId of allowedUserIds) {
    const user = await getProfile(userId);
    usersList.push({
      id: user.id,
      username: user.username,
      profilePicture: user.profilePicture,
    });
  }

  return usersList;
}

export async function updateChatTitle(chatId: string, title: string) {
  const object = {
    chatId,
    title,
  };
  const res = await fetch(`${API_URL}/update-title`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(object),
  });

  return res.json();
}
