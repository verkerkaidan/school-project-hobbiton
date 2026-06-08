import {
  Bell,
  Cog,
  User,
  MessageSquare,
  Rss,
  Search,
  Bookmark,
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
  { icon: Rss, label: "Feed", href: "/" },
  { icon: Search, label: "Discover", href: "/discover" },
  { icon: MessageSquare, label: "Chats", href: "/chats" },
  { icon: Bell, label: "Notifications", href: "/notifications" },
  { icon: Bookmark, label: "Saved", href: "/saved" },
  { icon: User, label: "Profile", href: "/profile" },
  { icon: Cog, label: "Settings", href: "/settings" },
  { icon: ShieldAlert, label: "Admin", href: "/admin", adminOnly: true },
];
