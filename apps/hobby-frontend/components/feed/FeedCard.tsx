"use client";
import {motion} from "framer-motion";
import Image from "next/image";
import { Post } from "@/types/posts";
import { useRouter } from "next/navigation";
import {ImageMenu} from "@/components/ui/ImageMenu";
import {useState} from "react";
import Cookies from "js-cookie";

export const FeedCard = ({ item, index }: { item: Post; index: number }) => {
  const aspectRatios = {
    short: "aspect-video",
    medium: "aspect-square",
    tall: "aspect-[3/4]",
  };
    const router = useRouter();

    const media = item.mediaSet?.[0];
    const imageSrc = media
        ? `data:${media.contentType};base64,${media.fileData}`
        : "https://images.unsplash.com/photo-1589998059171-988d887df646?q=80&w=1000";
  const [revealed, setRevealed] = useState(false);

  const showOverlay = media?.contentFlag === "RISK" && !revealed;

    const [aspectRatio] = useState(() => {
        const values = Object.values(aspectRatios);
        const randomIndex = Math.floor(Math.random() * values.length);
        return values[randomIndex];
    })


  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      whileInView={{ opacity: 1, scale: 1 }}
      viewport={{ once: true, amount: 0.3 }}
      transition={{ duration: 0.5, delay: index * 0.05 }}
      className="break-inside-avoid group mb-6"
      onClick={() => router.push(`/posts/${item.id}`)}
      >

      {/* Image Container */}
      {media && (
        <div
          className={`relative w-full overflow-hidden ${aspectRatio}`}
        >
            {/* 3-dot menu (top-right) */}
            <div className="absolute top-2 right-2 z-20">
                <ImageMenu relevantId={media.relevantId} userId={Cookies.get("userId")!} />
            </div>
          <Image
            src={imageSrc}
            alt={item.title}
            fill
            sizes={"(max-width: 768px) 100vw, 50vw"}
            className="object-cover group-hover:scale-105 transition-transform duration-700 ease-out"
          />
          <div className="absolute inset-0 bg-linear-to-t from-background/90 via-transparent to-transparent opacity-60" />
          {/* 🚨 Content Warning Overlay */}
          {showOverlay && (
              <div className="absolute inset-0 z-20 flex flex-col items-center justify-center bg-black/95 text-white text-center p-4">

                <div className="max-w-xs">
                  <h3 className="text-lg font-semibold mb-2">
                    Content Warning
                  </h3>
                  <p className="text-sm opacity-80 mb-4">
                    This image may contain sensitive or disturbing content.
                  </p>

                  <button
                      onClick={() => setRevealed(true)}
                      className="px-4 py-2 bg-white text-black rounded-full text-sm font-medium hover:bg-gray-200 transition"
                  >
                    View Image
                  </button>
                </div>

              </div>
          )}
        </div>
      )}

      {/* Content Area */}
      <div className="p-6 space-y-3">
        <div className="flex items-center gap-3">
          <span
            className={`w-2 h-2 rounded-full bg-blue-500 shadow-lg shadow-current/50`}
          />
          <span className="text-xs font-mono uppercase tracking-tighter text-accent">
            {item.hobbyType}
          </span>
        </div>
        <h2 className="text-2xl font-semibold group-hover:text-brand-light transition-colors duration-300">
          {item.title}
        </h2>
        <p className="text-accent text-sm leading-relaxed">
          {item.description}
        </p>
      </div>

      {/* Interaction Footer */}
      <div className="px-6 py-4 border-t border-white/5 flex justify-between items-center bg-white/2">
        <span className="text-[10px] text-stone-600 uppercase font-bold tracking-widest">
          Bekijk Post
        </span>
        <div className="flex gap-4 text-accent">
          <button className="hover:text-rose-500 transition-colors transform hover:scale-110">
            ♡
          </button>
          <button className="hover:text-brand-light transition-colors transform hover:scale-110">
            ⟲
          </button>
        </div>
      </div>
    </motion.div>
  );
};
