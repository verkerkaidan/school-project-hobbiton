export type NotificationType = "FOLLOW" | "USER_POST" | "RECOMMENDED_POST";

export interface Notification {
  id: string;
  userId: string;
  actorId: string | null;
  type: NotificationType;
  message: string;
  readAt: string | null;
  createdAt: string;
}

export interface NotificationsResponse {
  notifications: Notification[];
  total: number;
  limit: number;
  offset: number;
}

/** Map of notification type → enabled boolean returned from GET /notification-preferences */
export type NotificationPreferences = Record<NotificationType, boolean>;
