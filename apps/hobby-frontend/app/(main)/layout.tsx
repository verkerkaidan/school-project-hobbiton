import React from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { MainContent } from "@/components/layout/MainContent";
import { NotificationProvider } from "@/components/notifications/NotificationProvider";
import {ProfilePicProvider} from "@/components/ui/ProfilePicProvider";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
      <ProfilePicProvider>
        <NotificationProvider>
          <Sidebar />
          <MainContent>{children}</MainContent>
        </NotificationProvider>
      </ProfilePicProvider>
  );
}
