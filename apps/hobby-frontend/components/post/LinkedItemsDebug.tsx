"use client";

import { LinkedHobbyItem } from "@/types/posts";

export function LinkedItemsDebug({ items }: { items: LinkedHobbyItem[] | undefined }) {
    console.log("🔗 linkedItems received by client:", items);
    console.log("🔗 count:", items?.length ?? 0);

    if (!items?.length) {
        return <p className="text-xs text-red-400 mt-4">⚠️ No linked items (array empty or undefined)</p>;
    }

    return (
        <div className="mt-6 space-y-3">
            <h2 className="text-lg font-semibold">Linked Items</h2>
            {items.map((item) => {
                console.log("🎯 item:", item);
                return (
                    <div key={item.hobbyId ?? item.externalId} className="flex items-center gap-3 border border-gray rounded p-3">
                        {item.imageUrl && (
                            <img src={item.imageUrl} alt={item.displayName} className="w-12 h-12 object-cover rounded" />
                        )}
                        <div className="flex flex-col">
                            <span className="text-sm font-medium">{item.displayName}</span>
                            <span className="text-xs text-accent uppercase">{item.hobbyType}</span>
                        </div>
                    </div>
                );
            })}
        </div>
    );
}