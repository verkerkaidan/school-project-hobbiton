import {
  Bell,
  Cog,
  User,
  MessageSquare,
  Rss,
  Search,
  Bookmark,
<<<<<<< HEAD
} from "lucide-react";

export const SIDEBAR_NAV_ITEMS = [
=======
  ShieldAlert,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  icon: LucideIcon;
  label: string;
  href: string;
  badge?: number;
  /** When true, this item is only shown to admin users. */
  adminOnly?: boolean;
}

export const SIDEBAR_NAV_ITEMS: NavItem[] = [
>>>>>>> origin/main
  { icon: Rss, label: "Feed", href: "/" },
  { icon: Search, label: "Discover", href: "/discover" },
  { icon: MessageSquare, label: "Chats", href: "/chats", badge: 3 },
  { icon: Bell, label: "Notifications", href: "/notifications" },
  { icon: Bookmark, label: "Saved", href: "/saved" },
  { icon: User, label: "Profile", href: "/profile" },
  { icon: Cog, label: "Settings", href: "/settings" },
<<<<<<< HEAD
=======
  { icon: ShieldAlert, label: "Reports", href: "/admin", adminOnly: true },
>>>>>>> origin/main
];
