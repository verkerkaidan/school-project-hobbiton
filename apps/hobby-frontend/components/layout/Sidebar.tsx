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
import Image from "next/image";

export const Sidebar = () => {
  const { isActive, navigate } = useNavigation();
  const currentUser = useCurrentUser();
  const { unreadCount } = useNotifications();
  const [, setHasAnimated] = useState(false);
  const [hasSession, setHasSession] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(true);
  const firstRender = useRef(true);

  useEffect(() => {
    setHasSession(!!Cookies.get("userId"));
    if (firstRender.current) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setHasAnimated(true);
      firstRender.current = false;

      // Load theme preference from localStorage
      const savedTheme = localStorage.getItem("theme");
      const isDark = savedTheme ? savedTheme === "dark" : true;
      setIsDarkMode(isDark);
      applyTheme(isDark);
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

  const toggleTheme = () => {
    const newIsDarkMode = !isDarkMode;
    setIsDarkMode(newIsDarkMode);
    applyTheme(newIsDarkMode);
  };

  if (!currentUser && !hasSession) return null;

  const isAdmin = currentUser?.isAdmin ?? false;

  const visibleNavItems = SIDEBAR_NAV_ITEMS.filter(
    (item) => !item.adminOnly || isAdmin,
  );

  return (
    <aside className="fixed top-0 left-0 h-screen w-20 md:w-64 bg-background-secondary backdrop-blur-xl border-r border-white/5 flex flex-col items-center md:items-start py-8 px-4 z-50">
      {/* Logo */}
      <div className="flex justify-between w-full">
        <Link
          href="/"
          className="mb-16 p-3 bg-brand-muted rounded-2xl border border-brand/20 group"
        >
          <div
            className="w-8 h-8 rounded-full border-4 border-brand group-hover:border-brand-light transition-colors"
          />
        </Link>

        {/* Theme Toggle Buttons */}
        <div className="flex flex-col gap-2 mt-4">
          <button
            onClick={() => toggleTheme()}
            className={`px-2 py-2 rounded-lg font-medium text-sm transition-all duration-300 bg-brand-muted`}
          >
            {isDarkMode ?
              <div className="flex">
                <Image
                  src="/svgs/moon.svg"
                  alt="Edit title"
                  width={25}
                  height={25}
                  className=""
                />
                <p>dark</p>
              </div> :
                            <div className="flex">
                <Image
                  src="/svgs/sun.svg"
                  alt="Edit title"
                  width={25}
                  height={25}
                  className=""
                />
                <p>Light</p>
              </div>
            }
          </button>
        </div>
      </div>

      {/* Navigatie Links */}
      <nav className="flex-1 w-full space-y-3">
        {visibleNavItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);

          return (
            <Link
              key={item.label}
              href={item.href}
              className="block group"
              onClick={(e) => {
                e.preventDefault();
                navigate(item.href);
              }}
            >
              <motion.div
                className={`relative flex items-center gap-4 p-4 rounded-xl transition-all duration-300
                                ${active
                    ? "bg-brand-muted text-brand-light border border-brand/20"
                    : "text-accent hover:text-brand border border-transparent"
                  }`}
                whileHover={{ x: 5 }}
                transition={{ type: "spring", stiffness: 300, damping: 20 }}
              >
                <Icon
                  className="w-6 h-6 shrink-0"
                  strokeWidth={active ? 2 : 1.5}
                />
                <span className="text-sm font-medium hidden md:block">
                  {item.label}
                </span>

                {item.badge && (
                  <span className="absolute top-2 right-2 md:static md:top-auto md:right-auto ml-auto bg-rose-500 text-white text-[10px] font-bold w-5 h-5 flex items-center justify-center rounded-full">
                    {item.badge}
                  </span>
                )}

                {item.href === "/notifications" && unreadCount > 0 && (
                  <span className="absolute top-2 right-2 md:static md:top-auto md:right-auto ml-auto bg-rose-500 text-white text-[10px] font-bold w-5 h-5 flex items-center justify-center rounded-full">
                    {unreadCount > 99 ? "99+" : unreadCount}
                  </span>
                )}

                {active && (
                  <motion.div
                    layoutId="activeSideIndicator"
                    className="absolute left-0 top-3 bottom-3 w-1 bg-brand rounded-full"
                  />
                )}
              </motion.div>
            </Link>
          );
        })}
      </nav>

      {/* De nieuwe, schone User Profile Section via de component */}
      <UserAvatar
        name={currentUser?.username ?? "..."}
        role="Owner"
        profilePicture={currentUser?.profilePicture}
        isOnline={true}
      />


    </aside>
  );
};
