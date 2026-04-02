"use client";

import { useEffect, useState } from "react";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";

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
    const userId = Cookies.get("userId") ?? "";
    if (!userId) return;
    // Invalidate cache if a different user is now logged in (e.g. after re-login)
    if (cachedUser && cachedUser.id !== userId) {
      cachedUser = null;
    }
    if (cachedUser) return;
    getProfile(userId)
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
