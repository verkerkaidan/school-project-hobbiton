"use client";

import { ProfileView } from "@/components/profile/ProfileView";
import { CURRENT_USER_ID } from "@/lib/auth";

export default function ProfilePage() {
  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-7xl">
      <ProfileView userId={CURRENT_USER_ID} isOwnProfile={true} />
    </div>
  );
}
