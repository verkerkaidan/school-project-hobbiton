"use client";

import { ProfileView } from "@/components/profile/ProfileView";
import { useCurrentUser } from "@/hooks/useCurrentUser";

export default function ProfilePage() {
  const currentUser = useCurrentUser();
  const currentUserId = currentUser?.id ?? "";

  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-7xl">
      <ProfileView userId={currentUserId} isOwnProfile={true} />
    </div>
  );
}
