import Cookies from "js-cookie";
import {
  Notification,
  NotificationsResponse,
  NotificationPreferences,
} from "@/types/notifications";

const BASE_URL = process.env.NEXT_PUBLIC_NOTIFICATION_URL;

function authHeaders(): HeadersInit {
  const token = Cookies.get("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function fetchNotifications(
  limit = 10,
  offset = 0,
): Promise<NotificationsResponse> {
  const res = await fetch(
    `${BASE_URL}/notifications?limit=${limit}&offset=${offset}`,
    { headers: authHeaders() },
  );
  if (!res.ok) throw new Error("Failed to fetch notifications");
  return res.json();
}

export async function fetchUnreadCount(): Promise<number> {
  const res = await fetch(`${BASE_URL}/notifications/unread-count`, {
    headers: authHeaders(),
  });
  if (!res.ok) return 0;
  const data = await res.json();
  return data.count ?? 0;
}

export async function markNotificationRead(id: string): Promise<void> {
  await fetch(`${BASE_URL}/notifications/${id}/read`, {
    method: "PATCH",
    headers: authHeaders(),
  });
}

export async function markAllNotificationsRead(): Promise<void> {
  await fetch(`${BASE_URL}/notifications/read-all`, {
    method: "POST",
    headers: authHeaders(),
  });
}

export async function fetchNotificationPreferences(): Promise<NotificationPreferences> {
  const res = await fetch(`${BASE_URL}/notification-preferences`, {
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error("Failed to fetch notification preferences");
  return res.json();
}

export async function updateNotificationPreference(
  type: string,
  enabled: boolean,
): Promise<void> {
  const res = await fetch(`${BASE_URL}/notification-preferences/${type}`, {
    method: "PUT",
    headers: { ...authHeaders(), "Content-Type": "application/json" },
    body: JSON.stringify({ enabled }),
  });
  if (!res.ok) throw new Error("Failed to update notification preference");
}

export function buildWebSocketUrl(): string {
  const token = Cookies.get("token");
  const wsBase = (BASE_URL ?? "").replace(/^http/, "ws");
  return `${wsBase}/ws${token ? `?token=${token}` : ""}`;
}

export async function getPostSubscription(
  authorId: string,
): Promise<{ subscribed: boolean }> {
  const res = await fetch(`${BASE_URL}/user-post-subscriptions/${authorId}`, {
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error("Failed to fetch post subscription status");
  return res.json();
}

export async function togglePostSubscription(
  authorId: string,
): Promise<{ subscribed: boolean }> {
  const res = await fetch(`${BASE_URL}/user-post-subscriptions/${authorId}`, {
    method: "PUT",
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error("Failed to toggle post subscription");
  return res.json();
}

// Re-export types for convenience
export type { Notification, NotificationsResponse, NotificationPreferences };
