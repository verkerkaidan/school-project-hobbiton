"use client";

import { ProfileView } from "@/components/profile/ProfileView";
import { useCurrentUser } from "@/hooks/useCurrentUser";

export default function ProfilePage() {
  const currentUser = useCurrentUser();
  const currentUserId = currentUser?.id ?? "";

  return (
    <div className="pt-16 pb-20">
      <ProfileView userId={currentUserId} isOwnProfile />
    </div>
  );
}
