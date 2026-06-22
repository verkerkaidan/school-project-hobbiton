package com.postservice.postservice.services;

import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    public Set<MediaInstance> saveAsSetMediaInstanceFromSetUUID(
            Set<MediaInstance> mediaInstanceSet,
            Set<UUID> uuids,
            Post post
    ) {
        log.debug("saveAsSetMediaInstanceFromSetUUID called | postId={} uuids={} existing={}",
                post.getId(), uuids == null ? 0 : uuids.size(), mediaInstanceSet.size());

        if (uuids == null || uuids.isEmpty()) {
            log.info("No UUIDs provided, clearing media instances | postId={}", post.getId());
            mediaInstanceSet.clear();
            return mediaInstanceSet;
        }

        int beforeRemove = mediaInstanceSet.size();
        mediaInstanceSet.removeIf(
                existing -> !uuids.contains(existing.getInstanceId())
        );
        int removed = beforeRemove - mediaInstanceSet.size();
        if (removed > 0) {
            log.info("Removed stale media instances | postId={} removed={}", post.getId(), removed);
        }

        Set<UUID> existingInstanceIds = mediaInstanceSet
                .stream()
                .map(MediaInstance::getInstanceId)
                .collect(Collectors.toSet());

        int added = 0;
        for (UUID uuid : uuids) {
            if (!existingInstanceIds.contains(uuid)) {
                mediaInstanceSet.add(new MediaInstance(uuid, post));
                log.debug("Added new media instance | postId={} instanceId={}", post.getId(), uuid);
                added++;
            }
        }

        log.info("Media instances updated | postId={} added={} removed={} total={}",
                post.getId(), added, removed, mediaInstanceSet.size());

        return mediaInstanceSet;
    }
}