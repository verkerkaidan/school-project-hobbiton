"use client";

import { useEffect, useState } from "react";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";
import { isAdminToken } from "@/lib/jwt";
import { getCurrentUserEmail } from "@/lib/authenticationClient";
import {MediaSet} from "@/types/posts";

interface CurrentUser {
  id: string;
  username: string;
  profilePicture?: MediaSet;
  /** Email address, persisted in a cookie at login/register time. */
  email: string | null;
  /**
   * Derived from the `roles` claim in the JWT.
   * Always false until the auth service adds the claim to the token.
   */
  isAdmin: boolean;
}

// Module-level cache so the profile is only fetched once per session
let cachedUser: CurrentUser | null = null;

export function clearCurrentUser(): void {
  cachedUser = null;
}

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
        const token = Cookies.get("token") ?? "";
        cachedUser = {
          id: profile.id,
          username: profile.username,
          profilePicture: profile.profilePicture,
          email: getCurrentUserEmail(),
          isAdmin: isAdminToken(token),
        };
        setUser(cachedUser);
      })
      .catch(console.error);
  }, []);

  return user;
}
