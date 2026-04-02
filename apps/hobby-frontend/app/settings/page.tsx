"use client";

import { User, Bell, Lock, Shield } from "lucide-react";
import { SettingsSection } from "@/components/settings/SettingsSection";
import { BlockedUsersSection } from "@/components/settings/BlockedUsersSection";

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

        <SettingsSection
          icon={<Bell size={20} />}
          title="[Placeholder] Notifications"
          description="Choose what you get notified about"
        />

        <BlockedUsersSection />

        <SettingsSection
          icon={<Shield size={20} />}
          title="[Placeholder] Security"
          description="Password, two-factor authentication, and sessions"
        />
      </div>
    </div>
  );
}
