import { getProfile } from "@/queries/getProfile";
import { User } from "@/types/profile";
import api from "@/lib/axios";

const API_URL = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE + "/api";

export async function getChats(userId: string) {
  const res = await api.get(`${API_URL}/get-chats/${userId}`);
  return res.data;
}

export async function createChat(object: object) {
  const res = await api.post(`${API_URL}/create-chat`, object);
  return res.data;
}

export async function getChatTitle(chatId: string): Promise<string> {
  const res = await api.get(`${API_URL}/get-chat-title/${chatId}`);
  return res.data;
}

export async function getAllowedUsersFromChat(chatId: string): Promise<string[]> {
  const res = await api.get(`${API_URL}/allowed-users/${chatId}`);
  return res.data;
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
  const res = await api.put(`${API_URL}/update-title`, { chatId, title });
  return res.data;
}