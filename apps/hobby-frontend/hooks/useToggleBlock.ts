"use client";

import { useQueryClient } from "@tanstack/react-query";
import { toggleBlock } from "@/queries/getProfile";
import { FEED_QUERY_KEY } from "@/components/feed/Feed";

export function useToggleBlock() {
  const queryClient = useQueryClient();

  return async (currentUserId: string, targetUserId: string): Promise<boolean> => {
    const nowBlocking = await toggleBlock(currentUserId, targetUserId);
    queryClient.invalidateQueries({ queryKey: [FEED_QUERY_KEY] });
    return nowBlocking;
  };
}
