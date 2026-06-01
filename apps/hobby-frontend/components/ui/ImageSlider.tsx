'use client';

import { useState } from 'react';
import Image from 'next/image';
import Cookies from 'js-cookie';
import { ImageMenu } from '@/components/ui/ImageMenu';
import {MediaSet} from "@/types/posts";

interface ImageSliderProps {
    media: MediaSet[];
    aspectRatio?: string;
}

export default function ImageSlider({
                                        media,
                                        aspectRatio = 'aspect-video',
                                    }: ImageSliderProps) {
    const [current, setCurrent] = useState(0);
    const [revealedSlides, setRevealedSlides] = useState<Set<number>>(new Set());

    const total = media.length;
    const prev = () => setCurrent((c) => (c - 1 + total) % total);
    const next = () => setCurrent((c) => (c + 1) % total);
    const revealCurrent = () => setRevealedSlides((s) => new Set(s).add(current));

    if (!media.length) return null;

    return (
        <div className={`relative w-full overflow-hidden rounded-xl ${aspectRatio} mb-8 group`}>
            <div
                className="flex h-full transition-transform duration-500 ease-in-out"
                style={{ transform: `translateX(-${current * 100}%)` }}
            >
                {media.map((item, i) => {
                    const showOverlay = item.contentFlag === "RISK" && !revealedSlides.has(i);

                    return (
                        <div key={item.relevantId} className="relative w-full h-full shrink-0 overflow-hidden flex items-center justify-center">
                            <div className="absolute top-2 right-2 z-20">
                                <ImageMenu
                                    relevantId={item.relevantId}
                                    userId={Cookies.get('userId')!}
                                />
                            </div>

                            <Image
                                src={`data:${item.contentType};base64,${item.fileData}`}
                                alt={`Media ${i + 1}`}
                                width={0}
                                height={0}
                                sizes="(max-width: 768px) 100vw, 50vw"
                                className="w-full h-auto max-w-full max-h-full object-contain"
                            />

                            <div className="absolute inset-0 bg-linear-to-t from-background/90 via-transparent to-transparent opacity-60" />

                            {showOverlay && (
                                <div
                                    onClick={(e) => e.stopPropagation()}
                                    className="absolute inset-0 z-20 flex flex-col items-center justify-center text-white text-center p-4"
                                >
                                    <div className="absolute inset-0 backdrop-blur-3xl" />

                                    <div className="relative z-10 max-w-xs">
                                        <h3 className="text-lg font-semibold mb-2">Content Warning</h3>
                                        <p className="text-sm opacity-80 mb-4">
                                            This image may contain sensitive or disturbing content.
                                        </p>
                                        <button
                                            onClick={revealCurrent}
                                            className="px-4 py-2 bg-white text-black rounded-full text-sm font-medium hover:bg-gray-200 transition"
                                        >
                                            View Image
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

            {total > 1 && (
                <>
                    <button
                        onClick={prev}
                        className="absolute left-3 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/80 text-white rounded-full w-9 h-9 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-10"
                    >
                        ‹
                    </button>
                    <button
                        onClick={next}
                        className="absolute right-3 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/80 text-white rounded-full w-9 h-9 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-10"
                    >
                        ›
                    </button>

                    <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5 z-10">
                        {media.map((_, i) => (
                            <button
                                key={i}
                                onClick={() => setCurrent(i)}
                                className={`h-1.5 rounded-full transition-all ${
                                    i === current ? 'bg-white w-4' : 'bg-white/50 w-1.5'
                                }`}
                            />
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}