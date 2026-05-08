"use client";

import { useParams } from "next/navigation";
import { ProfileView } from "@/components/profile/ProfileView";
import { useCurrentUser } from "@/hooks/useCurrentUser";

export default function OtherProfilePage() {
  const params = useParams();
  const username = params?.username as string;
  const currentUser = useCurrentUser();

  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-7xl">
      <ProfileView
        username={username}
        isOwnProfile={!!currentUser && currentUser.username === username}
      />
    </div>
  );
}
