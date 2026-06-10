package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.*;
import com.apigateway.apigateway.modelDtos.media.MediaUploadResponse;
import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.MediaInstance;
import com.apigateway.apigateway.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class PostAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(PostAggregationService.class);
    private final InteractionClient interactionClient;

    @Value("${post.service.url}")
    private String postServiceUrl;
    @Value("${media.service.url}")
    private String mediaServiceUrl;

    private final PostClient postClient;
    private final MediaClient mediaClient;
    private final WebClient webClient;
    private final ProfileClient profileClient;
    private final ProfileConnectionClient profileConnectionClient;
    private final NotificationClient notificationClient;
    private final RecognitionService recognitionService;

    public PostAggregationService(PostClient postClient, MediaClient mediaClient, WebClient webClient,
                                  ProfileClient profileClient, ProfileConnectionClient profileConnectionClient,
                                  NotificationClient notificationClient, InteractionClient interactionClient,
                                  RecognitionService recognitionService) {
        this.postClient = postClient;
        this.mediaClient = mediaClient;
        this.webClient = webClient;
        this.profileClient = profileClient;
        this.profileConnectionClient = profileConnectionClient;
        this.notificationClient = notificationClient;
        this.interactionClient = interactionClient;
        this.recognitionService = recognitionService;
    }

    public List<Post> getPostsByHobbyType(HobbyType hobbyType) {
        return postClient.getPostsByHobbyType(hobbyType);
    }

    public List<Post> getAllPosts() {
        return postClient.getAllPosts();
    }

    /**
     * retrieve all mediaFiles from a list of instances
     * Retrieves the same file multiple times, if the instances refer to the same
     * file
     */
    public List<MediaDTO> getMedia(List<MediaInstance> instances) {
        List<UUID> instanceIds = instances.stream().map((MediaInstance instance) -> instance.instanceId).toList();
        List<MediaDTO> mediaDTOList = mediaClient.getMediaByInstanceIds(instanceIds);
        System.out.println("Retrieving media. Number of files: " + mediaDTOList.size());

        return mediaDTOList;
    }

    public Interaction getInteraction(UUID referenceId) {
        Interaction interaction = interactionClient.getInteraction(referenceId);
        System.out.println("Fetched interaction for referenceId: " + referenceId);
        return interaction;
    }

    /**
     * Forwards the create post from frontend to mediaservice to postservice.
     * After the post is saved, notifies all followers who have opted in to
     * per-author post notifications via the notification service batch endpoint.
     *
     * @param dto PostDTO with a list of datauri's for media
     * @return Post with a list of instance uuids for media
     */
    public Object forwardCreatePost(PostDTO dto) {

        // -----------------------------
        // 1. FIX: ensure type consistency
        // -----------------------------
        if (dto.hobbyType == null || dto.hobbyType.isBlank()) {
            // fallback based on class
            dto.hobbyType = dto.getClass()
                    .getSimpleName()
                    .replace("DTO", "")
                    .toUpperCase();
        }

        // 🔥 IMPORTANT: align DTO polymorphism field
        // (post-service expects "type")
        Map<String, Object> payload = new HashMap<>();

        payload.put("id", dto.id);
        payload.put("title", dto.title);
        payload.put("description", dto.description);
        payload.put("userId", dto.userId);
        payload.put("hobbyType", dto.hobbyType);

        // 🔥 CRITICAL: this is what fixes OTHER disappearing
        payload.put("type", dto.hobbyType);

        payload.put("mediaInstanceSet", dto.mediaInstanceSet);
        payload.put("linkedItems", dto.linkedItems);

        // -----------------------------
        // 2. MEDIA UPLOAD
        // -----------------------------
        Set<UUID> mediaInstanceSet = new HashSet<>();

        if (dto.mediaInstanceDTOSet != null && !dto.mediaInstanceDTOSet.isEmpty()) {
            mediaInstanceSet = uploadMedia(dto.mediaInstanceDTOSet, dto.userId);
            recognitionService.startRecognitionPipeline(dto.mediaInstanceDTOSet);
        } else {
            dto.mediaInstanceDTOSet = new ArrayList<>();
        }

        dto.mediaInstanceSet = mediaInstanceSet;
        payload.put("mediaInstanceSet", mediaInstanceSet);

        // -----------------------------
        // 3. SEND TO POST SERVICE
        // -----------------------------
        Object savedPost = webClient.post()
                .uri(postServiceUrl + "/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Object.class)
                .block();

        // -----------------------------
        // 4. SAFE RESPONSE PARSING
        // -----------------------------
        UUID savedPostId = null;
        String savedPostTitle = dto.title;

        try {
            if (savedPost instanceof Map<?, ?> map) {
                Object idObj = map.get("id");
                if (idObj != null) {
                    savedPostId = UUID.fromString(idObj.toString());
                }

                Object titleObj = map.get("title");
                if (titleObj != null && !titleObj.toString().isBlank()) {
                    savedPostTitle = titleObj.toString();
                }
            }
        } catch (Exception ignored) {}

        // -----------------------------
        // 5. NOTIFICATIONS
        // -----------------------------
        final UUID postId = savedPostId;
        final String postTitle = savedPostTitle;

        try {
            notifyFollowersOfNewPost(dto.userId, postId, postTitle);
        } catch (Exception e) {
            logger.warn("[PostAggregationService] Notification failed for userId={}: {}",
                    dto.userId, e.getMessage());
        }

        return savedPost;
    }

    /**
     * Handles the uploading of media and returns the list of instances Id's
     */
    private Set<UUID> uploadMedia(List<MultipartFile> files, UUID owner_id) {
        Set<UUID> mediaInstanceSet = new HashSet<>();

       MultiValueMap<String, HttpEntity<?>> multipartBody = new LinkedMultiValueMap<>();

        files.forEach((file) -> {
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
            multipartBody.add("files[]", new HttpEntity<>(file.getResource(), fileHeaders));
        });

        multipartBody.add("owner_id", new HttpEntity<>(owner_id.toString()));

        MediaUploadResponse response = webClient.post()
                .uri(mediaServiceUrl + "/media/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(multipartBody)
                .retrieve()
                .bodyToMono(MediaUploadResponse.class)
                .block();

        if (response != null) {
            response.results.stream()
                    .filter(r -> "ok".equals(r.status))
                    .map(r -> r.id)
                    .forEach(mediaInstanceSet::add);
        }
        for (UUID id : mediaInstanceSet) {
            System.out.println("Retrieved instanceId: " + id);
        }
        return mediaInstanceSet;
    }

    /**
     * Fetches the author's followers and their username, then sends a batch
     * USER_POST notification to all of them. The notification service will filter
     * down to only those who have (a) enabled USER_POST globally and (b) explicitly
     * subscribed to this author's posts.
     */
    private void notifyFollowersOfNewPost(UUID authorId, UUID postId, String postTitle) {
        Set<UUID> followers = profileConnectionClient.getFollowerIds(authorId);
        if (followers == null || followers.isEmpty()) {
            return;
        }

        String username;
        try {
            username = profileClient.getProfile(authorId).getUsername();
        } catch (Exception e) {
            logger.warn("[PostAggregationService] Could not resolve username for authorId={}", authorId);
            username = "Someone";
        }

        String titlePart = (postTitle != null && !postTitle.isBlank())
                ? " \"" + postTitle + "\""
                : " a new post";

        Map<String, Object> payload = new HashMap<>();
        payload.put("actorId", authorId.toString());
        payload.put("recipientIds", followers.stream().map(UUID::toString).toList());
        payload.put("type", "USER_POST");
        payload.put("message", "@" + username + " published" + titlePart);
        if (postId != null) {
            payload.put("referenceId", postId.toString());
        }

        notificationClient.batchCreateNotifications(payload);
    }

    public Post getPostById(UUID id) {
        return postClient.getPostById(id);
    }
}