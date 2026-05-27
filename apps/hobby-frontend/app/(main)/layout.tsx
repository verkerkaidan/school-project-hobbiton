import React from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { MainContent } from "@/components/layout/MainContent";
import { NotificationProvider } from "@/components/notifications/NotificationProvider";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <NotificationProvider>
      <Sidebar />
      <MainContent>{children}</MainContent>
    </NotificationProvider>
  );
}
