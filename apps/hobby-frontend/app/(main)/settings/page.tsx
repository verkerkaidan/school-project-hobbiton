"use client";

import { BlockedUsersSection } from "@/components/settings/BlockedUsersSection";
import { NotificationSettings } from "@/components/settings/NotificationSettings";
import { AccountSettings } from "@/components/settings/AccountSettings";
import { SecuritySettings } from "@/components/settings/SecuritySettings";
import { BugReportSection } from "@/components/settings/BugReportSection";
import { DeletionSection } from "@/components/settings/DeletionSection";

export default function SettingsPage() {
  return (
    <div className="pt-16 pb-20">
      <h1 className="text-2xl font-bold mb-8">Settings</h1>

      <div className="flex flex-col gap-4">
        <AccountSettings />
        <NotificationSettings />
        <BlockedUsersSection />
        <SecuritySettings />
        <BugReportSection />
        <DeletionSection />
      </div>

      <p className="mt-12 text-xs text-white/20 font-mono text-center">
        {process.env.NEXT_PUBLIC_APP_VERSION || "dev"}
      </p>
    </div>
  );
}
