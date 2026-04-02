'use client';

import {motion} from 'framer-motion';

export default function Loading() {
    return (
        <div
            className="fixed inset-0 z-100 flex flex-col items-center justify-center bg-background/50 backdrop-blur-md">
            <div className="relative flex items-center justify-center">
                {/* De buitenste draaiende ring */}
                <motion.div
                    className="w-20 h-20 border-t-2 border-l-2 border-brand rounded-full"
                    animate={{rotate: 360}}
                    transition={{
                        duration: 1,
                        repeat: Infinity,
                        ease: "linear"
                    }}
                />

                {/* De middelste pulserende ring */}
                <motion.div
                    className="absolute w-12 h-12 border-2 border-brand-muted rounded-full"
                    animate={{scale: [1, 1.2, 1], opacity: [0.3, 0.6, 0.3]}}
                    transition={{
                        duration: 2,
                        repeat: Infinity,
                        ease: "easeInOut"
                    }}
                />

                {/* Het centrale punt (je logo-stijl) */}
                <motion.div
                    className="absolute w-4 h-4 bg-brand rounded-full shadow-[0_0_15px_rgba(var(--brand-rgb),0.5)]"
                    initial={{scale: 0.8}}
                    animate={{scale: [0.8, 1.1, 0.8]}}
                    transition={{
                        duration: 1.5,
                        repeat: Infinity,
                        ease: "easeInOut"
                    }}
                />
            </div>

            {/* Optionele tekst */}
            <motion.p
                initial={{opacity: 0, y: 10}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.5}}
                className="mt-6 text-accent text-sm font-medium tracking-widest uppercase"
            >
                Loading Creativity...
            </motion.p>
        </div>
    );
}