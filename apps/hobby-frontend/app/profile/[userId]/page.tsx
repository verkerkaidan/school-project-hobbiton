"use client";

import { useParams } from "next/navigation";
import { ProfileView } from "@/components/profile/ProfileView";
import { CURRENT_USER_ID } from "@/lib/auth";

export default function OtherProfilePage() {
  const params = useParams();
  const userId = params?.userId as string;

  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-7xl">
      <ProfileView userId={userId} isOwnProfile={userId === CURRENT_USER_ID} />
    </div>
  );
}
