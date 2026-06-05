package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.NotificationClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.clients.ProfileConnectionClient;
import com.apigateway.apigateway.models.CreateReportRequest;
import com.apigateway.apigateway.models.User;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.ProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileAggregationService.class);

    private final ProfileClient profileClient;
    private final ProfileConnectionClient profileConnectionClient;
    private final PostClient postClient;
    private final NotificationClient notificationClient;

    public ProfileAggregationService(ProfileClient profileClient, ProfileConnectionClient profileConnectionClient,
            PostClient postClient, NotificationClient notificationClient) {
        this.profileClient = profileClient;
        this.profileConnectionClient = profileConnectionClient;
        this.postClient = postClient;
        this.notificationClient = notificationClient;
    }

    public User getProfileByUsername(String username) {
        ProfileResponse profileResponse = profileClient.getProfileByUsername(username);
        UUID userId = profileResponse.getUserId();
        Set<UUID> followers = profileConnectionClient.getFollowerIds(userId);
        Set<UUID> following = profileConnectionClient.getFollowingIds(userId);
        return new User(
                profileResponse.getUserId(),
                profileResponse.getUsername(),
                profileResponse.getBiography() != null ? profileResponse.getBiography() : "",
                profileResponse.getMediaPointer() != null ? profileResponse.getMediaPointer() : "/jpeg.png",
                followers.size(),
                following.size());
    }

    public User getProfile(UUID userId) {
        logger.info("[getProfile] Starting profile fetch for userId: {}", userId);
        try {
            logger.debug("[getProfile] Calling ProfileClient.getProfile()");
            ProfileResponse profileResponse = profileClient.getProfile(userId);
            logger.debug("[getProfile] ProfileClient returned: username={}, bio={}", profileResponse.getUsername(),
                    profileResponse.getBiography());

            logger.debug("[getProfile] Fetching follower and following counts");
            Set<UUID> followers = profileConnectionClient.getFollowerIds(userId);
            Set<UUID> following = profileConnectionClient.getFollowingIds(userId);
            logger.debug("[getProfile] Follower count: {}, Following count: {}", followers.size(), following.size());

            // Map ProfileResponse to User
            User profile = new User(
                    profileResponse.getUserId(),
                    profileResponse.getUsername(),
                    profileResponse.getBiography() != null ? profileResponse.getBiography() : "",
                    profileResponse.getMediaPointer() != null ? profileResponse.getMediaPointer() : "/jpeg.png",
                    followers.size(),
                    following.size());

            logger.info("[getProfile] Successfully created User for userId: {}", userId);
            return profile;
        } catch (Exception e) {
            logger.error("[getProfile] FAILED to fetch profile for userId: {} - Exception: {}", userId,
                    e.getClass().getName(), e);
            throw new RuntimeException("Failed to fetch profile for user: " + userId, e);
        }
    }

    public List<User> getFollowers(UUID userId) {
        logger.info("[getFollowers] Starting followers fetch for userId: {}", userId);
        try {
            logger.debug("[getFollowers] Fetching follower IDs");
            Set<UUID> followerIds = profileConnectionClient.getFollowerIds(userId);
            logger.debug("[getFollowers] Retrieved {} follower IDs", followerIds.size());

            List<User> followers = followerIds.stream()
                    .map(this::convertUUIDToUser)
                    .filter(user -> user != null)
                    .collect(Collectors.toList());

            logger.info("[getFollowers] Successfully converted {} follower IDs to User objects for userId: {}",
                    followers.size(), userId);
            return followers;
        } catch (Exception e) {
            logger.error("[getFollowers] FAILED to fetch followers for userId: {} - Exception: {}", userId,
                    e.getClass().getName(), e);
            throw new RuntimeException("Failed to fetch followers for user: " + userId, e);
        }
    }

    public List<User> getFollowing(UUID userId) {
        logger.info("[getFollowing] Starting following fetch for userId: {}", userId);
        try {
            logger.debug("[getFollowing] Fetching following IDs");
            Set<UUID> followingIds = profileConnectionClient.getFollowingIds(userId);
            logger.debug("[getFollowing] Retrieved {} following IDs", followingIds.size());

            List<User> following = followingIds.stream()
                    .map(this::convertUUIDToUser)
                    .filter(user -> user != null)
                    .collect(Collectors.toList());

            logger.info("[getFollowing] Successfully converted {} following IDs to User objects for userId: {}",
                    following.size(), userId);
            return following;
        } catch (Exception e) {
            logger.error("[getFollowing] FAILED to fetch following for userId: {} - Exception: {}", userId,
                    e.getClass().getName(), e);
            throw new RuntimeException("Failed to fetch following for user: " + userId, e);
        }
    }

    public Boolean toggleFollow(UUID currentUserId, UUID targetUserId) {
        logger.info("[toggleFollow] currentUserId={} toggling follow on targetUserId={}", currentUserId, targetUserId);
        try {
            Boolean isNowFollowing = profileConnectionClient.toggleFollow(targetUserId, currentUserId);
            logger.info("[toggleFollow] Result: isNowFollowing={}", isNowFollowing);
            if (Boolean.TRUE.equals(isNowFollowing)) {
                sendFollowNotification(targetUserId, currentUserId);
            }
            return isNowFollowing;
        } catch (Exception e) {
            logger.error("[toggleFollow] FAILED for currentUserId={}, targetUserId={} - {}", currentUserId,
                    targetUserId, e.getMessage(), e);
            throw new RuntimeException("Failed to toggle follow", e);
        }
    }

    private void sendFollowNotification(UUID targetUserId, UUID actorUserId) {
        try {
            String username = null;
            try {
                ProfileResponse actorProfile = profileClient.getProfile(actorUserId);
                if (actorProfile != null) {
                    username = actorProfile.getUsername();
                }
            } catch (Exception e) {
                logger.warn("[sendFollowNotification] Could not fetch username for actor {}: {}", actorUserId,
                        e.getMessage());
            }
            String message = (username != null)
                    ? "@" + username + " started following you"
                    : "Someone started following you";

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", targetUserId.toString());
            payload.put("actorId", actorUserId.toString());
            payload.put("type", "FOLLOW");
            payload.put("message", message);

            notificationClient.createNotification(payload);
            logger.debug("[sendFollowNotification] Follow notification sent: actor={} -> target={}", actorUserId,
                    targetUserId);
        } catch (Exception e) {
            logger.warn("[sendFollowNotification] Failed to send follow notification (actor={}, target={}): {}",
                    actorUserId, targetUserId, e.getMessage());
        }
    }

    private User convertUUIDToUser(UUID userId) {
        try {
            logger.debug("[convertUUIDToUser] Converting UUID to User: {}", userId);
            ProfileResponse profileResponse = profileClient.getProfile(userId);
            String profilePictureUrl = profileResponse.getMediaPointer() != null ? profileResponse.getMediaPointer()
                    : "/jpeg.png";
            User user = new User(userId, profileResponse.getUsername(), profilePictureUrl);
            logger.debug("[convertUUIDToUser] Successfully converted: {}", userId);
            return user;
        } catch (Exception e) {
            logger.warn("[convertUUIDToUser] Failed to convert UUID to User: {} - {}", userId, e.getMessage());
            return null;
        }
    }

    public Boolean isFollowing(UUID currentUserId, UUID targetUserId) {
        try {
            Set<UUID> followingIds = profileConnectionClient.getFollowingIds(currentUserId);
            return followingIds.contains(targetUserId);
        } catch (Exception e) {
            logger.warn("[isFollowing] Could not determine follow state: {}", e.getMessage());
            return false;
        }
    }

    public List<User> getBlockedUsers(UUID userId) {
        logger.info("[getBlockedUsers] Starting blocked users fetch for userId: {}", userId);
        try {
            Set<UUID> blockedIds = profileConnectionClient.getBlockedIds(userId);
            logger.debug("[getBlockedUsers] Retrieved {} blocked IDs", blockedIds.size());
            List<User> blockedUsers = blockedIds.stream()
                    .map(this::convertUUIDToUser)
                    .filter(user -> user != null)
                    .collect(Collectors.toList());
            logger.info("[getBlockedUsers] Successfully converted {} blocked IDs to User objects for userId: {}",
                    blockedUsers.size(), userId);
            return blockedUsers;
        } catch (Exception e) {
            logger.error("[getBlockedUsers] FAILED to fetch blocked users for userId: {} - Exception: {}", userId,
                    e.getClass().getName(), e);
            throw new RuntimeException("Failed to fetch blocked users for user: " + userId, e);
        }
    }

    public Boolean isBlocking(UUID currentUserId, UUID targetUserId) {
        try {
            Set<UUID> blockedIds = profileConnectionClient.getBlockedIds(currentUserId);
            return blockedIds.contains(targetUserId);
        } catch (Exception e) {
            logger.warn("[isBlocking] Could not determine block state: {}", e.getMessage());
            return false;
        }
    }

    public Boolean toggleBlock(UUID currentUserId, UUID targetUserId) {
        logger.info("[toggleBlock] currentUserId={} toggling block on targetUserId={}", currentUserId, targetUserId);
        try {
            Boolean isNowBlocking = profileConnectionClient.toggleBlock(targetUserId, currentUserId);
            logger.info("[toggleBlock] Result: isNowBlocking={}", isNowBlocking);
            return isNowBlocking;
        } catch (Exception e) {
            logger.error("[toggleBlock] FAILED for currentUserId={}, targetUserId={} - {}", currentUserId,
                    targetUserId, e.getMessage(), e);
            throw new RuntimeException("Failed to toggle block", e);
        }
    }

    public void createReport(UUID initiatorId, UUID targetId, String reason, String description) {
        logger.info("[createReport] initiatorId={} reporting targetId={} for reason={}", initiatorId, targetId, reason);
        try {
            profileConnectionClient.createReport(new CreateReportRequest(initiatorId, targetId, reason, description));
            logger.info("[createReport] Report submitted successfully");
        } catch (Exception e) {
            logger.error("[createReport] FAILED for initiatorId={}, targetId={} - {}", initiatorId,
                    targetId, e.getMessage(), e);
            throw new RuntimeException("Failed to create report", e);
        }
    }

    public List<Post> getUserPosts(UUID userId) {
        logger.info("[getUserPosts] Starting user posts fetch for userId: {}", userId);
        try {
            logger.debug("[getUserPosts] Calling PostClient.getUserPosts()");
            List<Post> posts = postClient.getUserPosts(userId);
            logger.info("[getUserPosts] Successfully retrieved {} posts for userId: {}", posts.size(), userId);
            return posts;
        } catch (Exception e) {
            logger.error("[getUserPosts] FAILED to fetch posts for userId: {} - Exception: {}", userId,
                    e.getClass().getName(), e);
            throw new RuntimeException("Failed to fetch posts for user: " + userId, e);
        }
    }
}
