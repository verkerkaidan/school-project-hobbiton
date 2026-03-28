'use client';
import {motion} from 'framer-motion';
import Link from 'next/link';
import {Post} from '@/types';
import Image from "next/image";

export const FeedCard = ({item, index}: { item: Post, index: number }) => {
    const aspectRatios = {
        short: 'aspect-video',
        medium: 'aspect-square',
        tall: 'aspect-[3/4]'
    };

    return (
        <motion.div
            initial={{opacity: 0, scale: 0.95}}
            whileInView={{opacity: 1, scale: 1}}
            viewport={{once: true, amount: 0.3}}
            transition={{duration: 0.5, delay: index * 0.05}}
            className="break-inside-avoid group mb-6"
        >
            <Link href={item.href}
                  className="block relative overflow-hidden rounded-post glass-card glass-card-hover">

                {/* Image Container */}
                <div className={`relative w-full overflow-hidden ${aspectRatios[item.size]}`}>
                    <Image src={item.image} alt={item.title} fill
                           sizes={"(max-width: 768px) 100vw, 50vw"}
                           className="object-cover group-hover:scale-105 transition-transform duration-700 ease-out"
                    />
                    <div
                        className="absolute inset-0 bg-linear-to-t from-background/90 via-transparent to-transparent opacity-60"/>
                </div>

                {/* Content Area */}
                <div className="p-6 space-y-3">
                    <div className="flex items-center gap-3">
                        <span className={`w-2 h-2 rounded-full ${item.color} shadow-lg shadow-current/50`}/>
                        <span className="text-xs font-mono uppercase tracking-tighter text-accent">{item.type}</span>
                    </div>
                    <h2 className="text-2xl font-semibold group-hover:text-brand-light transition-colors duration-300">
                        {item.title}
                    </h2>
                    <p className="text-accent text-sm leading-relaxed">
                        {item.content}
                    </p>
                </div>

                {/* Interaction Footer */}
                <div className="px-6 py-4 border-t border-white/5 flex justify-between items-center bg-white/2">
                    <span className="text-[10px] text-stone-600 uppercase font-bold tracking-widest">
                        Bekijk Post
                    </span>
                    <div className="flex gap-4 text-accent">
                        <button className="hover:text-rose-500 transition-colors transform hover:scale-110">♡</button>
                        <button className="hover:text-brand-light transition-colors transform hover:scale-110">⟲
                        </button>
                    </div>
                </div>
            </Link>
        </motion.div>
    );
};