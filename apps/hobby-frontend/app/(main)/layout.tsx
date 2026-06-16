import React from "react";
import { NotificationProvider } from "@/components/notifications/NotificationProvider";
import { ChatNotificationProvider } from "@/components/notifications/ChatNotificationProvider";
import { ProfilePicProvider } from "@/components/ui/ProfilePicProvider";
import { ResponsiveNavigation } from "@/components/layout/ResponsiveNavigation";
import {ErrorToastProvider} from "@/components/ui/ErrorToastProvider";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
      <ErrorToastProvider>
        <ProfilePicProvider>
          <NotificationProvider>
            <ChatNotificationProvider>
              <div className="min-h-dvh md:flex">
                <div className="md:w-64 md:shrink-0">
                  <ResponsiveNavigation />
                </div>
                <main className="flex-1 pb-16 md:pb-0 flex justify-center px-4">
                  <div className="w-full max-w-5xl">{children}</div>
                </main>
              </div>
            </ChatNotificationProvider>
          </NotificationProvider>
        </ProfilePicProvider>
      </ErrorToastProvider>
  );
}
