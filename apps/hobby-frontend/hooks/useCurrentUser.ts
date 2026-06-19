"use client";

import { useEffect, useSyncExternalStore } from "react";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";
import { isAdminToken } from "@/lib/jwt";
import { getCurrentUserEmail } from "@/lib/authenticationClient";
import { MediaSet } from "@/types/posts";

interface CurrentUser {
  id: string;
  username: string;
  profilePicture?: MediaSet;
  /** Email address, persisted in a cookie at login/register time. */
  email: string | null;
  token: string;
  /**
   * Derived from the `roles` claim in the JWT.
   * Always false until the auth service adds the claim to the token.
   */
  isAdmin: boolean;
}

let cachedUser: CurrentUser | null = null;
const listeners = new Set<() => void>();

function subscribe(callback: () => void) {
  listeners.add(callback);
  return () => listeners.delete(callback);
}

function getSnapshot() {
  return cachedUser;
}

function setCachedUser(user: CurrentUser | null) {
  cachedUser = user;
  listeners.forEach((l) => l());
}

export function clearCurrentUser(): void {
  setCachedUser(null);
}

export function updateUsername(name: string): CurrentUser | null {
  if (!cachedUser) return null;
  setCachedUser({ ...cachedUser, username: name });
  return cachedUser;
}

export function useCurrentUser(): CurrentUser | null {
  const user = useSyncExternalStore(subscribe, getSnapshot, () => null);

  useEffect(() => {
    const userId = Cookies.get("userId") ?? "";
    if (!userId) return;
    if (cachedUser && cachedUser.id !== userId) {
      setCachedUser(null);
    }
    if (cachedUser) return;
    getProfile(userId)
      .then((profile) => {
        const token = Cookies.get("token") ?? "";
        setCachedUser({
          id: profile.id,
          username: profile.username,
          profilePicture: profile.profilePicture,
          email: getCurrentUserEmail(),
          isAdmin: isAdminToken(token),
          token: token,
        });
      })
      .catch(console.error);
  }, []);

  return user;
}
