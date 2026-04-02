"use client";

import { useParams } from "next/navigation";
import { ProfileView } from "@/components/profile/ProfileView";
import { useCurrentUser } from "@/hooks/useCurrentUser";

export default function OtherProfilePage() {
  const params = useParams();
  const userId = params?.userId as string;
  const currentUser = useCurrentUser();
  const currentUserId = currentUser?.id ?? "";

  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-7xl">
      <ProfileView userId={userId} isOwnProfile={userId === currentUserId} />
    </div>
  );
}
