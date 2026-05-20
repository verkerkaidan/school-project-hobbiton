package com.apigateway.apigateway.service;

import com.apigateway.apigateway.modelDtos.media.MediaEntryDTO;
import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.NotificationClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.clients.ProfileConnectionClient;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.models.MediaInstance;
import com.apigateway.apigateway.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class PostAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(PostAggregationService.class);

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

    public PostAggregationService(PostClient postClient, MediaClient mediaClient, WebClient webClient,
            ProfileClient profileClient, ProfileConnectionClient profileConnectionClient,
            NotificationClient notificationClient) {
        this.postClient = postClient;
        this.mediaClient = mediaClient;
        this.webClient = webClient;
        this.profileClient = profileClient;
        this.profileConnectionClient = profileConnectionClient;
        this.notificationClient = notificationClient;
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
    public List<MediaDTO> getMedia(List<MediaInstance> instanceIds) {
        List<MediaDTO> mediaDTOList = new ArrayList<>();
        for (MediaInstance instance : instanceIds) {
            System.out.println(instance.instanceId);
            MediaDTO dto = mediaClient.getMediaByInstanceId(instance.instanceId);
            dto.setRelevantId(instance.instanceId);
            mediaDTOList.add(dto);
        }

        return mediaDTOList;
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
        Set<UUID> mediaInstanceSet = new HashSet<>();
        dto.mediaInstanceDTOSet.forEach((instance) -> mediaInstanceSet.add(webClient.post()
                .uri(mediaServiceUrl + "/media/" + dto.userId)
                .bodyValue(new MediaEntryDTO(instance))
                .retrieve()
                .bodyToMono(UUID.class)
                .block()));

        dto.mediaInstanceSet = mediaInstanceSet;
        Object savedPost = webClient.post()
                .uri(postServiceUrl + "/posts")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Object.class)
                .block();

        // Extract the saved post's ID and title from the response.
        // Using the response (canonical DB values) avoids any deserialization
        // edge-cases
        // that could leave dto fields null.
        UUID savedPostId = null;
        String savedPostTitle = dto.title; // fallback: what was sent
        try {
            if (savedPost instanceof Map<?, ?> savedPostMap) {
                Object idObj = savedPostMap.get("id");
                if (idObj != null) {
                    savedPostId = UUID.fromString(idObj.toString());
                }
                Object titleObj = savedPostMap.get("title");
                if (titleObj != null && !titleObj.toString().isBlank()) {
                    savedPostTitle = titleObj.toString();
                }
            }
        } catch (Exception ignored) {
        }

        if (savedPostTitle == null || savedPostTitle.isBlank()) {
            logger.warn(
                    "[PostAggregationService] Post title is blank for userId={} — notification will use fallback text",
                    dto.userId);
        }

        // Fire-and-forget: notify subscribers; never let notification errors fail the
        // request
        final UUID postId = savedPostId;
        final String postTitle = savedPostTitle;
        try {
            notifyFollowersOfNewPost(dto.userId, postId, postTitle);
        } catch (Exception e) {
            logger.warn("[PostAggregationService] Failed to send post notifications for userId={}: {}",
                    dto.userId, e.getMessage());
        }

        return savedPost;
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