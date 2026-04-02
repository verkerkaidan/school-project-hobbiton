"use client";

import { useEffect, useState } from "react";
import { getProfile } from "@/queries/getProfile";
import { CURRENT_USER_ID } from "@/lib/auth";

interface CurrentUser {
  id: string;
  username: string;
  profilePictureUrl: string;
}

// Module-level cache so the profile is only fetched once per session
let cachedUser: CurrentUser | null = null;

export function useCurrentUser(): CurrentUser | null {
  const [user, setUser] = useState<CurrentUser | null>(cachedUser);

  useEffect(() => {
    if (cachedUser) return;
    getProfile(CURRENT_USER_ID)
      .then((profile) => {
        cachedUser = {
          id: profile.id,
          username: profile.username,
          profilePictureUrl: profile.profilePictureUrl,
        };
        setUser(cachedUser);
      })
      .catch(console.error);
  }, []);

  return user;
}
