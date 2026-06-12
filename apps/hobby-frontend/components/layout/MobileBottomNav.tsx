"use client";

import React, { useMemo, useRef, useState, useEffect } from "react";
import Link from "next/link";
import Cookies from "js-cookie";
import { motion, AnimatePresence } from "framer-motion";
import { SIDEBAR_NAV_ITEMS } from "@/constants/navigation";
import { useNavigation } from "../navigation/useNavigation";
import { useNotifications } from "@/components/notifications/NotificationProvider";
import { useChatNotifications } from "@/components/notifications/ChatNotificationProvider";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { useRouter } from "next/navigation";
import { MoreHorizontal, X, LogOut } from "lucide-react";
import Image from "next/image";
import { useTheme } from "@/hooks/useTheme";

export const MobileBottomNav = () => {
  const { isActive, navigate } = useNavigation();
  const { unreadCount } = useNotifications();
  const { unreadTotal } = useChatNotifications();
  const currentUser = useCurrentUser();
  const router = useRouter();
  const { isDarkMode, toggleTheme } = useTheme();

  const [expanded, setExpanded] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);

  const isAdmin = currentUser?.isAdmin ?? false;

  const allItems = useMemo(
    () => SIDEBAR_NAV_ITEMS.filter((item) => !item.adminOnly || isAdmin),
    [isAdmin],
  );

  const mainItems = allItems.slice(0, 4);
  const extraItems = allItems.slice(4);

  const handleLogout = () => {
    Cookies.remove("userId");
    setExpanded(false);
    router.push("/login");
  };

  const getBadge = (href: string) => {
    if (href === "/chats") return unreadTotal;
    if (href === "/notifications") return unreadCount;
    return 0;
  };

  useEffect(() => {
    if (!expanded) return;

    const handleClickOutside = (e: MouseEvent) => {
      if (!menuRef.current) return;
      if (!menuRef.current.contains(e.target as Node)) {
        setExpanded(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [expanded]);

  return (
    <>
      {/* BACKDROP (FULL SCREEN, FIXED) */}
      <AnimatePresence>
        {expanded && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-background/60 backdrop-blur-sm z-40"
          />
        )}
      </AnimatePresence>

      {/* EXPANDED MENU (FIXED OUTSIDE BAR) */}
      <AnimatePresence>
        {expanded && (
          <motion.div
            ref={menuRef}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="
              fixed bottom-16 left-0 right-0 z-50
              bg-background-secondary/95
              backdrop-blur-xl
              border-t border-white/10
              pb-3
            "
          >
            <div className="px-4 py-3 flex items-center justify-between border-b border-white/10">
              <button
                onClick={() => setExpanded(false)}
                className="text-xs text-accent/70"
              >
                Close
              </button>

              <div className="flex items-center gap-2">
                <button
                  onClick={toggleTheme}
                  className="flex items-center gap-2 text-sm font-medium px-3 py-1.5 rounded-lg bg-brand-muted text-accent"
                >
                  {isDarkMode ? (
                    <>
                      <Image
                        src="/svgs/moon.svg"
                        alt="dark"
                        width={18}
                        height={18}
                      />
                      <span className="text-xs hidden sm:block">Dark</span>
                    </>
                  ) : (
                    <>
                      <Image
                        src="/svgs/sun.svg"
                        alt="light"
                        width={18}
                        height={18}
                      />
                      <span className="text-xs hidden sm:block">Light</span>
                    </>
                  )}
                </button>

                <button
                  onClick={handleLogout}
                  className="text-red-400 text-sm font-medium flex items-center gap-2"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </div>

            {extraItems.length > 0 && (
              <div className="grid grid-cols-4 gap-2 p-3">
                {extraItems.map((item) => {
                  const Icon = item.icon;
                  const active = isActive(item.href);

                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      onClick={(e) => {
                        e.preventDefault();
                        navigate(item.href);
                        setExpanded(false);
                      }}
                      className="flex flex-col items-center text-accent/70"
                    >
                      <div
                        className={`
                          p-2 rounded-xl
                          ${active ? "bg-brand-muted text-brand" : ""}
                        `}
                      >
                        <Icon className="w-6 h-6" />
                      </div>
                      <span className="text-[10px] mt-1">{item.label}</span>
                    </Link>
                  );
                })}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* BOTTOM BAR */}
      <div
        className="
          fixed bottom-0 left-0 right-0 z-50
          h-16
          bg-background-secondary/95
          backdrop-blur-xl
          border-t border-white/10
        "
      >
        <nav className="flex items-center justify-around h-16">
          {mainItems.map((item, index) => {
            const Icon = item.icon;
            const active = isActive(item.href);
            const badge = getBadge(item.href);
            const isToggle = index === mainItems.length - 1;

            const handleClick = (e: React.MouseEvent) => {
              if (isToggle) {
                e.preventDefault();
                setExpanded((v) => !v);
                return;
              }

              e.preventDefault();
              navigate(item.href);
              setExpanded(false);
            };

            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={handleClick}
                className="flex flex-col items-center justify-center w-full"
              >
                <div
                  className={`
                    relative p-2 rounded-xl
                    ${active && !isToggle ? "bg-brand-muted text-brand" : "text-accent/70"}
                  `}
                >
                  {isToggle ? (
                    expanded ? (
                      <X className="w-6 h-6" />
                    ) : (
                      <MoreHorizontal className="w-6 h-6" />
                    )
                  ) : (
                    <>
                      <Icon className="w-6 h-6" />
                      {badge > 0 && (
                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] w-4 h-4 flex items-center justify-center rounded-full">
                          {badge > 99 ? "99+" : badge}
                        </span>
                      )}
                    </>
                  )}
                </div>

                {!isToggle && (
                  <span className="text-[10px] text-accent/70">
                    {item.label}
                  </span>
                )}
              </Link>
            );
          })}
        </nav>
      </div>
    </>
  );
};
