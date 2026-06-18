package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.InteractionClient;
import com.apigateway.apigateway.clients.NotificationClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.modelDtos.interaction.CommentDTO;
import com.apigateway.apigateway.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class InteractionAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(InteractionAggregationService.class);

    private final InteractionClient interactionClient;
    private final PostClient postClient;
    private final ProfileClient profileClient;
    private final NotificationClient notificationClient;

    public InteractionAggregationService(InteractionClient interactionClient,
            PostClient postClient,
            ProfileClient profileClient,
            NotificationClient notificationClient) {
        this.interactionClient = interactionClient;
        this.postClient = postClient;
        this.profileClient = profileClient;
        this.notificationClient = notificationClient;
    }

    /** Batch check: returns the subset of referenceIds that userId has liked — 1 DB query. */
    public Set<UUID> batchDoesUserLike(UUID userId, List<UUID> referenceIds) {
        try {
            return interactionClient.batchDoesUserLike(userId, referenceIds);
        } catch (Exception e) {
            logger.error("[InteractionAggregationService batchDoesUserLike] Failed: {}", e.getMessage());
            return Set.of();
        }
    }

    public boolean doesLikeExist(UUID userId, UUID referenceId) {
        try {
            return interactionClient.doesLikeExist(userId, referenceId);
        } catch (RuntimeException e) {
            logger.error("[INTERACTIONAGGREGATIONSERVICE doesLikeExist] Could not send request to interactionService --- \n{}", e.getMessage());
            return false;
        }
    }

    /**
     * Toggles the like state for a post. When the result is a new like (true),
     * the post owner receives a LIKE notification — unless they liked their own
     * post.
     */
    public boolean toggleLike(UUID actorId, UUID referenceId) {
        try {
            boolean nowLiked = interactionClient.toggleLike(actorId, referenceId);
            if (nowLiked) {
                sendLikeNotification(actorId, referenceId);
            }
            return nowLiked;
        } catch (Exception e) {
            logger.error("[INTERACTIONAGGREGATIONSERVICE toggleLike] Could not reach interactionService --- \n{}", e.getMessage());
            throw new RuntimeException("Responsible service offline");
        }
    }

    /**
     * Creates a comment on a post and notifies the post owner — unless the
     * commenter is the post owner themselves.
     */
    public UUID createComment(CommentDTO comment) {
        UUID commentId = interactionClient.createComment(comment);
        sendCommentNotification(comment.userId, comment.referenceId, comment.comment);
        return commentId;
    }

    private void sendLikeNotification(UUID actorId, UUID postId) {
        try {
            Post post = postClient.getPostById(postId);
            UUID ownerId = post.getUserId();

            // Don't notify on self-likes
            if (ownerId.equals(actorId)) {
                return;
            }

            String actorUsername = resolveUsername(actorId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", ownerId.toString());
            payload.put("actorId", actorId.toString());
            payload.put("type", "LIKE");
            payload.put("message", "@" + actorUsername + " liked your post");
            payload.put("referenceId", postId.toString());

            notificationClient.createNotification(payload);
            logger.debug("[sendLikeNotification] LIKE notification sent: actor={} -> postOwner={}", actorId, ownerId);
        } catch (Exception e) {
            logger.warn("[sendLikeNotification] Failed to send LIKE notification (actor={}, post={}): {}",
                    actorId, postId, e.getMessage());
        }
    }

    private void sendCommentNotification(UUID actorId, UUID postId, String commentContent) {
        try {
            Post post = postClient.getPostById(postId);
            UUID ownerId = post.getUserId();

            // Don't notify on self-comments
            if (ownerId.equals(actorId)) {
                return;
            }

            String actorUsername = resolveUsername(actorId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", ownerId.toString());
            payload.put("actorId", actorId.toString());
            payload.put("type", "COMMENT");
            payload.put("message", "@" + actorUsername + " commented on your post");
            payload.put("content", commentContent);
            payload.put("referenceId", postId.toString());

            notificationClient.createNotification(payload);
            logger.debug("[sendCommentNotification] COMMENT notification sent: actor={} -> postOwner={}", actorId,
                    ownerId);
        } catch (Exception e) {
            logger.warn("[sendCommentNotification] Failed to send COMMENT notification (actor={}, post={}): {}",
                    actorId, postId, e.getMessage());
        }
    }

    private String resolveUsername(UUID userId) {
        try {
            return profileClient.getProfile(userId).getUsername();
        } catch (Exception e) {
            logger.warn("[InteractionAggregationService] Could not resolve username for userId={}", userId);
            return "Someone";
        }
    }
}
