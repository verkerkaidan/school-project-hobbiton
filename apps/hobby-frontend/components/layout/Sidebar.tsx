'use client';

import React, {useEffect, useRef, useState} from 'react';
import Link from 'next/link';
import {SIDEBAR_NAV_ITEMS} from '@/constants/navigation';
import {UserAvatar} from '@/components/ui/UserAvatar';
import {useNavigation} from '../navigation/useNavigation';
import {motion} from "framer-motion";

export const Sidebar = () => {
    const {isActive, navigate} = useNavigation();
    const [, setHasAnimated] = useState(false);
    const firstRender = useRef(true);

    useEffect(() => {
        if (firstRender.current) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setHasAnimated(true);
            firstRender.current = false;
        }
    }, []);

    return (
        <aside
            className="fixed top-0 left-0 h-screen w-20 md:w-64 bg-black/20 backdrop-blur-xl border-r border-white/5 flex flex-col items-center md:items-start py-8 px-4 z-50"
        >
            {/* Logo */}
            <Link href="/" className="mb-16 p-3 bg-brand-muted rounded-2xl border border-brand/20 group">
                <motion.div
                    animate={{rotate: [0, 360, 0]}}
                    transition={{duration: 10, repeat: Infinity, ease: "linear"}}
                    className="w-8 h-8 rounded-full border-4 border-brand group-hover:border-brand-light transition-colors"
                />
            </Link>

            {/* Navigatie Links */}
            <nav className="flex-1 w-full space-y-3">
                {SIDEBAR_NAV_ITEMS.map((item) => {
                    const Icon = item.icon;
                    const active = isActive(item.href);

                    return (
                        <Link key={item.label} href={item.href} className="block group" onClick={e => {
                            e.preventDefault();
                            navigate(item.href);
                        }}>
                            <motion.div
                                className={`relative flex items-center gap-4 p-4 rounded-xl transition-all duration-300
                                ${active
                                    ? 'bg-brand-muted text-brand-light border border-brand/20'
                                    : 'hover:bg-white/5 text-accent hover:text-white border border-transparent'
                                }`}
                                whileHover={{x: 5}}
                                transition={{type: 'spring', stiffness: 300, damping: 20}}
                            >
                                <Icon className="w-6 h-6 shrink-0" strokeWidth={active ? 2 : 1.5}/>
                                <span className="text-sm font-medium hidden md:block">{item.label}</span>

                                {item.badge && (
                                    <span
                                        className="absolute top-2 right-2 md:static md:top-auto md:right-auto ml-auto bg-rose-500 text-white text-[10px] font-bold w-5 h-5 flex items-center justify-center rounded-full">
                                        {item.badge}
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
                name="Demiiee"
                role="Owner"
                imageUrl="/jpeg.png"
                isOnline={true}
            />

        </aside>
    );
};