import React from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { MainContent } from "@/components/layout/MainContent";
import { NotificationProvider } from "@/components/notifications/NotificationProvider";
import { ChatNotificationProvider } from "@/components/notifications/ChatNotificationProvider";
import {ProfilePicProvider} from "@/components/ui/ProfilePicProvider";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ProfilePicProvider>
      <NotificationProvider>
        <ChatNotificationProvider>
          <Sidebar />
          <MainContent>{children}</MainContent>
        </ChatNotificationProvider>
      </NotificationProvider>
    </ProfilePicProvider>

  );
}
