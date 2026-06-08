package com.postservice.postservice.services;

import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    public Set<MediaInstance> saveAsSetMediaInstanceFromSetUUID(
        Set<MediaInstance> mediaInstanceSet,
        Set<UUID> uuids,
        Post post
    ) {
        if (uuids == null || uuids.isEmpty()) {
            mediaInstanceSet.clear();
            return mediaInstanceSet;
        }

        mediaInstanceSet.removeIf(
            existing -> !uuids.contains(existing.getInstanceId())
        );

        Set<UUID> existingInstanceIds = mediaInstanceSet
            .stream()
            .map(MediaInstance::getInstanceId)
            .collect(Collectors.toSet());

        for (UUID uuid : uuids) {
            if (!existingInstanceIds.contains(uuid)) {
                mediaInstanceSet.add(new MediaInstance(uuid, post));
            }
        }

        return mediaInstanceSet;
    }
}
