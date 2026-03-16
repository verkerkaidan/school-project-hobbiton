package com.postservice.postservice.services;

import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostServiceTest {

    private PostService postService;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        postService = new PostService();
        mockPost = new Post() {};
    }

    @Test
    @DisplayName("When UUIDs is null, the existing media instance set should be cleared and returned empty")
    void saveAsSetMediaInstance_nullUuids_clearsAndReturnsEmptySet() {
        MediaInstance existing = new MediaInstance(UUID.randomUUID(), mockPost);
        Set<MediaInstance> mediaInstanceSet = new HashSet<>(Set.of(existing));

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(mediaInstanceSet, null, mockPost);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("When UUIDs is an empty set, the existing media instance set should be cleared and returned empty")
    void saveAsSetMediaInstance_emptyUuids_clearsAndReturnsEmptySet() {
        MediaInstance existing = new MediaInstance(UUID.randomUUID(), mockPost);
        Set<MediaInstance> mediaInstanceSet = new HashSet<>(Set.of(existing));

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(mediaInstanceSet, new HashSet<>(), mockPost);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("When all UUIDs are new, a MediaInstance should be created and added for each one")
    void saveAsSetMediaInstance_newUuids_addsNewMediaInstances() {
        Set<MediaInstance> mediaInstanceSet = new HashSet<>();
        UUID newUuid1 = UUID.randomUUID();
        UUID newUuid2 = UUID.randomUUID();

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(
                mediaInstanceSet, Set.of(newUuid1, newUuid2), mockPost
        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(MediaInstance::getInstanceId)
                .containsExactlyInAnyOrder(newUuid1, newUuid2);
    }

    @Test
    @DisplayName("When a UUID already exists in the set, no duplicate MediaInstance should be added")
    void saveAsSetMediaInstance_uuidAlreadyExists_doesNotAddDuplicate() {
        UUID existingUuid = UUID.randomUUID();
        MediaInstance existing = new MediaInstance(existingUuid, mockPost);
        Set<MediaInstance> mediaInstanceSet = new HashSet<>(Set.of(existing));

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(
                mediaInstanceSet, Set.of(existingUuid), mockPost
        );

        assertThat(result).hasSize(1);
        assertThat(result).extracting(MediaInstance::getInstanceId).containsExactly(existingUuid);
    }

    @Test
    @DisplayName("When a UUID is no longer in the incoming set, its MediaInstance should be removed")
    void saveAsSetMediaInstance_removesStaleUuids() {
        UUID keepUuid = UUID.randomUUID();
        UUID removeUuid = UUID.randomUUID();

        MediaInstance keepInstance = new MediaInstance(keepUuid, mockPost);
        MediaInstance staleInstance = new MediaInstance(removeUuid, mockPost);
        Set<MediaInstance> mediaInstanceSet = new HashSet<>(Set.of(keepInstance, staleInstance));

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(
                mediaInstanceSet, Set.of(keepUuid), mockPost
        );

        assertThat(result).hasSize(1);
        assertThat(result).extracting(MediaInstance::getInstanceId).containsExactly(keepUuid);
    }

    @Test
    @DisplayName("When UUIDs contain a mix of existing, stale, and new entries, stale ones are removed and new ones are added")
    void saveAsSetMediaInstance_mixedUuids_addsNewAndRemovesStale() {
        UUID existingUuid = UUID.randomUUID();
        UUID staleUuid = UUID.randomUUID();
        UUID newUuid = UUID.randomUUID();

        MediaInstance existingInstance = new MediaInstance(existingUuid, mockPost);
        MediaInstance staleInstance = new MediaInstance(staleUuid, mockPost);
        Set<MediaInstance> mediaInstanceSet = new HashSet<>(Set.of(existingInstance, staleInstance));

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(
                mediaInstanceSet, Set.of(existingUuid, newUuid), mockPost
        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(MediaInstance::getInstanceId)
                .containsExactlyInAnyOrder(existingUuid, newUuid);
    }

    @Test
    @DisplayName("When a new MediaInstance is created, it should be linked to the Post passed into the method")
    void saveAsSetMediaInstance_newMediaInstanceLinkedToCorrectPost() {
        UUID newUuid = UUID.randomUUID();

        Set<MediaInstance> result = postService.saveAsSetMediaInstanceFromSetUUID(
                new HashSet<>(), Set.of(newUuid), mockPost
        );

        assertThat(result)
                .first()
                .extracting(MediaInstance::getPost)
                .isEqualTo(mockPost);
    }
}