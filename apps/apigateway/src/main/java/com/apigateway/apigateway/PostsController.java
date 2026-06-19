package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.InteractionClient;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.interaction.InteractionBatchResult;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.models.AdminProfileSummaryResponse;
import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.LinkedHobbyItem;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.service.MediaService;
import com.apigateway.apigateway.service.PostAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PostsController {

    private static final int DEFAULT_PAGE_SIZE = 16;

    private final PostAggregationService postService;
    private final MediaService mediaService;
    private final MediaClient mediaClient;
    private final InteractionClient interactionClient;
    private final ProfileClient profileClient;

    private static final Logger log = LoggerFactory.getLogger(PostsController.class);

    public PostsController(PostAggregationService postService, MediaService mediaService,
                           MediaClient mediaClient, InteractionClient interactionClient,
                           ProfileClient profileClient) {
        this.postService = postService;
        this.mediaService = mediaService;
        this.mediaClient = mediaClient;
        this.interactionClient = interactionClient;
        this.profileClient = profileClient;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // SchemaMapping fallbacks (used by getPostById and other single-post queries)
    // ───────────────────────────────────────────────────────────────────────────

    /**
     * Returns pre-fetched media if the batch path already set it, otherwise falls
     * back to an individual media-service call (used by getPostById etc.).
     */
    @SchemaMapping(typeName = "Post", field = "mediaSet")
    public List<MediaDTO> mediaSet(Post post) {
        if (post.getMediaSet() != null) return post.getMediaSet();
        if (post.getMediaInstanceSet() == null || post.getMediaInstanceSet().isEmpty()) {
            return new ArrayList<>();
        }
        return mediaService.getMedia(post.getMediaInstanceSet());
    }

    /**
     * Returns pre-fetched interaction if the batch path already set it, otherwise
     * falls back to an individual interaction-service call.
     */
    @SchemaMapping(typeName = "Post", field = "interaction")
    public Interaction interaction(Post post) {
        if (post.getInteraction() != null) return post.getInteraction();
        if (post.getId() == null) {
            log.warn("Post has null ID, skipping interaction fetch");
            return null;
        }
        return postService.getInteraction(post.getId());
    }

    /**
     * Returns the pre-fetched author username; falls back to a single profile-service
     * call for queries that don't go through the batch path (e.g. getPostById).
     */
    @SchemaMapping(typeName = "Post", field = "authorUsername")
    public String authorUsername(Post post) {
        if (post.getAuthorUsername() != null) return post.getAuthorUsername();
        if (post.getUserId() == null) return null;
        try {
            return profileClient.getProfile(post.getUserId()).getUsername();
        } catch (Exception e) {
            log.warn("Could not resolve authorUsername for userId={}", post.getUserId());
            return null;
        }
    }

    @SchemaMapping(typeName = "Post", field = "linkedItems")
    public List<LinkedHobbyItem> linkedItems(Post post) {
        List<LinkedHobbyItem> items = post.getLinkedItems();
        return items != null ? items : new ArrayList<>();
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Query resolvers
    // ───────────────────────────────────────────────────────────────────────────

    @QueryMapping
    public List<Post> getPostsByHobbyType(@Argument HobbyType hobbyType) {
        List<Post> postSet = new ArrayList<>(postService.getPostsByHobbyType(hobbyType));
        postSet.forEach(p -> log.info("Post class = {}", p.getClass().getName()));
        return postSet;
    }

    /**
     * Paginated feed query.
     * Fetches one page of posts, then enriches them with media, interactions,
     * and author usernames using 3 batch calls — one to each downstream service —
     * instead of N per-post calls.
     *
     * Total service calls per request:
     *   post-service      × 1  (paginated list)
     *   media-service     × 1  (all media for all posts)
     *   interaction-service × 1  (all interactions for all posts)
     *   profile-service   × 1  (all unique author usernames)
     */
    @QueryMapping
    public List<Post> getPosts(@Argument Integer page, @Argument Integer size, @Argument UUID currentUserId) {
        int p = page != null ? page : 0;
        int s = size != null ? size : DEFAULT_PAGE_SIZE;

        List<Post> posts;
        if (currentUserId != null) {
            posts = new ArrayList<>(postService.getPagedPostsForUser(p, s, currentUserId));
        } else {
            posts = new ArrayList<>(postService.getPagedPosts(p, s));
        }
        if (posts.isEmpty()) return posts;

        batchEnrichPosts(posts);
        return posts;
    }

    @QueryMapping
    public Post getPostById(@Argument UUID id) {
        return postService.getPostById(id);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Mutation / REST
    // ───────────────────────────────────────────────────────────────────────────

    @ResponseBody
    @PostMapping(value = "/create-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Set<UUID>> createPost(
            @RequestPart("data") PostDTO dto,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String authorization) {
        dto.mediaInstanceDTOSet = files;
        Set<UUID> response = postService.forwardCreatePost(dto, authorization);
        return ResponseEntity.ok(response);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Private batch enrichment helper
    // ───────────────────────────────────────────────────────────────────────────

    /**
     * Enriches a list of posts in-place with media, interactions, and author
     * usernames. Each downstream service is called exactly once, regardless of
     * how many posts are in the list.
     */
    private void batchEnrichPosts(List<Post> posts) {

        // ── 1. Media ──────────────────────────────────────────────────────────
        // Collect every media instance ID from every post in one flat list.
        List<UUID> allInstanceIds = posts.stream()
                .filter(p -> p.getMediaInstanceSet() != null && !p.getMediaInstanceSet().isEmpty())
                .flatMap(p -> p.getMediaInstanceSet().stream())
                .map(mi -> mi.instanceId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, MediaDTO> mediaById = Collections.emptyMap();
        if (!allInstanceIds.isEmpty()) {
            try {
                List<MediaDTO> allMedia = mediaClient.getMediaByInstanceIds(allInstanceIds);
                mediaById = allMedia.stream()
                        .filter(m -> m.relevantId != null)
                        .collect(Collectors.toMap(m -> m.relevantId, m -> m, (a, b) -> a));
            } catch (Exception e) {
                log.warn("[PostsController batchEnrichPosts] Media batch call failed: {}", e.getMessage());
            }
        }
        final Map<UUID, MediaDTO> mediaLookup = mediaById;

        posts.forEach(post -> {
            if (post.getMediaInstanceSet() != null && !post.getMediaInstanceSet().isEmpty()) {
                List<MediaDTO> postMedia = post.getMediaInstanceSet().stream()
                        .map(mi -> mediaLookup.get(mi.instanceId))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                post.setMediaSet(postMedia);
            } else {
                post.setMediaSet(new ArrayList<>());
            }
        });

        // ── 2. Interactions ───────────────────────────────────────────────────
        List<UUID> postIds = posts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<UUID, Interaction> interactionLookup = Collections.emptyMap();
        if (!postIds.isEmpty()) {
            try {
                List<InteractionBatchResult> batchResults = interactionClient.batchGetInteractions(postIds);
                interactionLookup = batchResults.stream()
                        .filter(r -> r.referenceId != null)
                        .collect(Collectors.toMap(r -> r.referenceId, InteractionBatchResult::toInteraction));
            } catch (Exception e) {
                log.warn("[PostsController batchEnrichPosts] Interaction batch call failed: {}", e.getMessage());
            }
        }
        final Map<UUID, Interaction> interactionMap = interactionLookup;
        posts.forEach(post -> post.setInteraction(
                interactionMap.getOrDefault(post.getId(), new Interaction(0, List.of()))
        ));

        // ── 3. Author usernames ───────────────────────────────────────────────
        List<UUID> uniqueAuthorIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> usernameLookup = Collections.emptyMap();
        if (!uniqueAuthorIds.isEmpty()) {
            try {
                List<AdminProfileSummaryResponse> profiles = profileClient.getProfilesByIds(uniqueAuthorIds);
                usernameLookup = profiles.stream()
                        .filter(p -> p.userId != null && p.username != null)
                        .collect(Collectors.toMap(p -> p.userId, p -> p.username));
            } catch (Exception e) {
                log.warn("[PostsController batchEnrichPosts] Profile batch call failed: {}", e.getMessage());
            }
        }
        final Map<String, String> usernameMap = usernameLookup;
        posts.forEach(post -> {
            if (post.getUserId() != null) {
                post.setAuthorUsername(usernameMap.get(post.getUserId().toString()));
            }
        });
    }
}
