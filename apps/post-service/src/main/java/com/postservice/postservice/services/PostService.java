package com.postservice.postservice.services;

import com.postservice.postservice.dao.PostRepository;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    /**
     * automatically converts set of UUIDS to set of MediaInstance
     *
     * @param mediaInstanceSet
     * @param uuids
     * @param post
     * @return
     */
    public Set<MediaInstance> saveAsSetMediaInstanceFromSetUUID(Set<MediaInstance> mediaInstanceSet, Set<UUID> uuids, Post post) {
        if (uuids == null || uuids.isEmpty()) {
            mediaInstanceSet.clear();
            return mediaInstanceSet;
        }

        mediaInstanceSet.removeIf(existing -> !uuids.contains(existing.getInstanceId()));

        Set<UUID> existingInstanceIds = mediaInstanceSet.stream()
                .map(MediaInstance::getInstanceId)
                .collect(Collectors.toSet());

        for (UUID uuid : uuids) {
            if (!existingInstanceIds.contains(uuid)) {
                mediaInstanceSet.add(new MediaInstance(uuid, post));
            }
        }

        return mediaInstanceSet;
    }

    public Post updatePost(PostDTO postDTO, Post post) {
        post.setDescription(postDTO.description);
        post.setTitle(postDTO.title);
        post.setMediaInstanceSet(this.saveAsSetMediaInstanceFromSetUUID(post.getMediaInstanceSet(), postDTO.mediaInstanceSet, post));

        return post;
    }
}
