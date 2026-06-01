"use client";

import React, { useEffect, useState } from "react";
import Cookies from "js-cookie";
import { useCurrentUser } from "@/hooks/useCurrentUser";

export const MainContent = ({ children }: { children: React.ReactNode }) => {
  const currentUser = useCurrentUser();
  const [hasSession, setHasSession] = useState(false);

  useEffect(() => {
    setHasSession(!!Cookies.get("userId"));
  }, []);

  const withSidebar = !!(currentUser || hasSession);
  return (
    <main className={`flex-1 ${withSidebar ? "ml-20 md:ml-64" : ""}`}>
      {children}
    </main>
  );
};
