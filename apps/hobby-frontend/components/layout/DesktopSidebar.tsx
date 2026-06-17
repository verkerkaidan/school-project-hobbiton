"use client";

import React, { useEffect, useRef, useState } from "react";
import Link from "next/link";
import Cookies from "js-cookie";
import { SIDEBAR_NAV_ITEMS } from "@/constants/navigation";
import { UserAvatar } from "@/components/ui/UserAvatar";
import { useNavigation } from "../navigation/useNavigation";
import { motion } from "framer-motion";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { useNotifications } from "@/components/notifications/NotificationProvider";
import { useChatNotifications } from "@/components/notifications/ChatNotificationProvider";
import Image from "next/image";
import { useTheme } from "@/hooks/useTheme";

export const DesktopSidebar = () => {
  const { isActive, navigate } = useNavigation();
  const currentUser = useCurrentUser();
  const { unreadCount } = useNotifications();
  const { unreadTotal: chatUnreadTotal } = useChatNotifications();

  const [, setHasAnimated] = useState(false);
  const [hasSession, setHasSession] = useState(false);
  const { isDarkMode, toggleTheme } = useTheme();
  const firstRender = useRef(true);

  useEffect(() => {
    setHasSession(!!Cookies.get("userId"));

    if (firstRender.current) {
      setHasAnimated(true);
      firstRender.current = false;
    }
  }, []);

  const applyTheme = (isDark: boolean) => {
    const htmlElement = document.documentElement;

    if (isDark) {
      htmlElement.classList.remove("light-mode");
      localStorage.setItem("theme", "dark");
    } else {
      htmlElement.classList.add("light-mode");
      localStorage.setItem("theme", "light");
    }
  };

  if (!currentUser && !hasSession) return null;

  const isAdmin = currentUser?.isAdmin ?? false;

  const visibleNavItems = SIDEBAR_NAV_ITEMS.filter(
    (item) => !item.adminOnly || isAdmin,
  );

  return (
    <aside
      className="
        fixed top-0 left-0
        h-dvh
        w-16 md:w-64
        bg-background-secondary backdrop-blur-xl
        border-r border-white/5
        flex flex-col
        items-center md:items-start
        py-4 md:py-8 px-2 md:px-4
        z-50
        overflow-x-hidden
      "
    >
      {/* Top */}
      <div className="flex flex-col md:flex-row items-center justify-between w-full gap-3 shrink-0">
        <Link
          href="/"
          className="p-2 md:p-3 bg-brand-muted rounded-2xl border border-brand/20 group"
        >
          <div className="w-6 h-6 md:w-8 md:h-8 rounded-full border-4 border-brand group-hover:border-brand-light transition-colors" />
        </Link>

        <button
          onClick={toggleTheme}
          className="p-2 rounded-lg bg-brand-muted flex items-center gap-2"
        >
          {isDarkMode ? (
            <>
              <Image src="/svgs/moon.svg" alt="dark" width={20} height={20} />
              <p className="hidden md:block">Dark</p>
            </>
          ) : (
            <>
              <Image src="/svgs/sun.svg" alt="light" width={20} height={20} />
              <p className="hidden md:block">Light</p>
            </>
          )}
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 w-full space-y-2 md:space-y-3 mt-6 overflow-y-auto overflow-x-hidden">
        {visibleNavItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);

          return (
            <Link
              key={item.label}
              href={item.href}
              onClick={(e) => {
                e.preventDefault();
                navigate(item.href);
              }}
              className="block group"
            >
              <motion.div
                className={`
                  relative flex items-center gap-3 md:gap-4
                  p-2 md:p-4 rounded-xl
                  transition-all duration-300
                  ${
                    active
                      ? "bg-brand-muted text-brand-light border border-brand/20"
                      : "text-accent hover:text-brand border border-transparent"
                  }
                `}
                whileHover={{ x: 4 }}
              >
                <Icon
                  className="w-5 h-5 md:w-6 md:h-6 shrink-0"
                  strokeWidth={active ? 2 : 1.5}
                />

                <span className="hidden md:block text-sm font-medium">
                  {item.label}
                </span>

                {item.href === "/chats" && chatUnreadTotal > 0 && (
                  <span className="absolute top-1 right-1 md:static ml-auto bg-rose-500 text-white text-[10px] font-bold w-5 h-5 flex items-center justify-center rounded-full">
                    {chatUnreadTotal > 99 ? "99+" : chatUnreadTotal}
                  </span>
                )}

                {item.href === "/notifications" && unreadCount > 0 && (
                  <span className="absolute top-1 right-1 md:static ml-auto bg-rose-500 text-white text-[10px] font-bold w-5 h-5 flex items-center justify-center rounded-full">
                    {unreadCount > 99 ? "99+" : unreadCount}
                  </span>
                )}

                {active && (
                  <motion.div
                    layoutId="activeSideIndicator"
                    className="absolute left-0 top-2 bottom-2 w-1 bg-brand rounded-full"
                  />
                )}
              </motion.div>
            </Link>
          );
        })}
      </nav>

      {/* Bottom user section (FIXED) */}
      <div className="mt-auto w-full flex flex-col items-center md:items-start gap-3 pt-4">
        <div className="scale-90 md:scale-100 origin-bottom">
          <UserAvatar
            name={currentUser?.username ?? "..."}
            role={currentUser?.isAdmin ? "Admin" : "Hobbiist"}
            profilePicture={currentUser?.profilePicture}
            isOnline={true}
          />
        </div>
      </div>
    </aside>
  );
};
