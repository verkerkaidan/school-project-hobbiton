const API_URL = process.env.NEXT_PUBLIC_WS_CHAT_SERVICE_URL;

export function buildChatNotificationWebSocketUrl(userId: string, token: string): string {
  if (!API_URL) return "";
  const base = API_URL.endsWith("/") ? API_URL.slice(0, -1) : API_URL;
  return `${base}/ws/notifications/${encodeURIComponent(userId)}?token=${token}`;
}
