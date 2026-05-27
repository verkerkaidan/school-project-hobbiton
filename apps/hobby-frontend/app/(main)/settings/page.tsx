"use client";

import { User, Lock, Shield } from "lucide-react";
import { SettingsSection } from "@/components/settings/SettingsSection";
import { BlockedUsersSection } from "@/components/settings/BlockedUsersSection";
import { NotificationSettings } from "@/components/settings/NotificationSettings";

export default function SettingsPage() {
  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-2xl">
      <h1 className="text-2xl font-bold mb-8">Settings</h1>

      <div className="flex flex-col gap-4">
        <SettingsSection
          icon={<User size={20} />}
          title="[Placeholder] Account"
          description="Edit your profile, username, and bio"
        />

        <SettingsSection
          icon={<Lock size={20} />}
          title="[Placeholder] Privacy"
          description="Control who can see your posts and profile"
        />

        <NotificationSettings />

        <BlockedUsersSection />

        <SettingsSection
          icon={<Shield size={20} />}
          title="[Placeholder] Security"
          description="Password, two-factor authentication, and sessions"
        />
      </div>

      <p className="mt-12 text-xs text-white/20 font-mono text-center">
        {process.env.NEXT_PUBLIC_APP_VERSION || "dev"}
      </p>
    </div>
  );
}
