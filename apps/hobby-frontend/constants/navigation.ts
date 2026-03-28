import {Cog, User, MessageSquare, Rss, Search, Bookmark} from 'lucide-react';

export const SIDEBAR_NAV_ITEMS = [
    {icon: Rss, label: 'Feed', href: '/'},
    {icon: Search, label: 'Discover', href: '/discover'},
    {icon: MessageSquare, label: 'Chats', href: '/chats', badge: 3},
    {icon: Bookmark, label: 'Saved', href: '/saved'},
    {icon: User, label: 'Profile', href: '/profile'},
    {icon: Cog, label: 'Settings', href: '/settings'},
];