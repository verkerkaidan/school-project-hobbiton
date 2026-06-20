package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.*;
import com.apigateway.apigateway.enums.ContentFlag;
import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.modelDtos.profile.UpdateProfileDTO;
import com.apigateway.apigateway.models.*;
import com.apigateway.apigateway.models.media.MediaInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileAggregationService.class);

    private final ProfileClient profileClient;
    private final ProfileConnectionClient profileConnectionClient;
    private final PostClient postClient;
    private final NotificationClient notificationClient;
    private final MediaClient mediaClient;
    private final RecognitionClient recognitionClient;
    private final MediaService mediaService;

    public ProfileAggregationService(ProfileClient profileClient, ProfileConnectionClient profileConnectionClient,
                                     PostClient postClient, NotificationClient notificationClient, MediaClient mediaClient, RecognitionClient recognitionClient, MediaService mediaService) {
        this.profileClient = profileClient;
        this.profileConnectionClient = profileConnectionClient;
        this.postClient = postClient;
        this.notificationClient = notificationClient;
        this.mediaClient = mediaClient;
        this.recognitionClient = recognitionClient;
        this.mediaService = mediaService;
    }

    private Set<UUID> safeGetFollowerIds(UUID userId) {
        try {
            return profileConnectionClient.getFollowerIds(userId);
        } catch (Exception e) {
            logger.warn("[ProfileAggregationService] Failed to fetch follower IDs for {}: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    private Set<UUID> safeGetFollowingIds(UUID userId) {
        try {
            return profileConnectionClient.getFollowingIds(userId);
        } catch (Exception e) {
            logger.warn("[ProfileAggregationService] Failed to fetch following IDs for {}: {}", userId, e.getMessage());
            return Set.of();
        }
    }

    public User getProfileByUsername(String username) {
        ProfileResponse profileResponse = profileClient.getProfileByUsername(username);
        UUID userId = profileResponse.getUserId();
        Set<UUID> followers = safeGetFollowerIds(userId);
        Set<UUID> following = safeGetFollowingIds(userId);

        return new User(
                profileResponse.getUserId(),
                profileResponse.getUsername(),
                profileResponse.getBiography() != null ? profileResponse.getBiography() : "",
                retrieveProfilePicture(profileResponse),
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
            Set<UUID> followers = safeGetFollowerIds(userId);
            Set<UUID> following = safeGetFollowingIds(userId);
            logger.debug("[getProfile] Follower count: {}, Following count: {}", followers.size(), following.size());

            // Map ProfileResponse to User
            User profile = new User(
                    profileResponse.getUserId(),
                    profileResponse.getUsername(),
                    profileResponse.getBiography() != null ? profileResponse.getBiography() : "",
                    retrieveProfilePicture(profileResponse),
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

    public List<AdminProfileSummaryResponse> getAllProfiles() {
        List<AdminProfileSummaryResponse> response = profileClient.getAllProfiles();
        return finaliseProfiles(response);
    }

    public List<AdminProfileSummaryResponse> fuzzySearchProfilesByUsername(String query) {
        List<AdminProfileSummaryResponse> response = profileClient.fuzzySearchProfilesByUsername(query);
        logger.error("[PROFILEaGGREGATIONsERVICE fuzzySearch] retrieved profiles: {}", response.size());
        return finaliseProfiles(response);
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

            User user = new User(userId, profileResponse.getUsername(), retrieveProfilePicture(profileResponse));
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

    public void updateProfilePicture(UUID userId, List<MultipartFile> files, String authorization, UUID oldId) {
        List<ContentFlagUpdateDTO> dtos = new ArrayList<>();
        files.forEach((MultipartFile file) -> {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                byte[] bytes = file.getBytes();
                byte[] recHash = digest.digest(bytes);
                String base64Hash = Base64.getEncoder().encodeToString(recHash);
                String base64File = Base64.getEncoder().encodeToString(bytes);

                dtos.add(new ContentFlagUpdateDTO(base64File, base64Hash));
            } catch (Exception e) {
                System.out.println("Error occurred while hashing a file: " + e.getMessage());
            }
        });
        List<ContentFlagUpdateDTO> results = new ArrayList<ContentFlagUpdateDTO>();
        try {
            results = recognitionClient.assignContentFlags(dtos);
        } catch (RuntimeException e) {
            results.add(new ContentFlagUpdateDTO(ContentFlag.UNASSIGNED));
        }

        if (results.getFirst().contentFlag == ContentFlag.RISK) {
            throw new RuntimeException("AI-model flagged uploaded picture as against TOS");
        }
        else {
            updateProfilePicturePipeline(files, userId, authorization, oldId);
        }
    }

    private MediaDTO retrieveProfilePicture(ProfileResponse profileResponse) {
        try {
            List<MediaDTO> mediaDtos = new ArrayList<>();
            if (profileResponse.getMediaPointer() != null) {
                mediaDtos = mediaClient.getMediaByInstanceIds(
                        List.of(
                                UUID.fromString(profileResponse.getMediaPointer())
                        ));
            }
            return mediaDtos.isEmpty() ? null : mediaDtos.getFirst();
        } catch (RuntimeException e) {
            logger.error("[PROFILEAGGREGATIONSERVICE retrieveProfilePicture] Could not send request to mediaServiceElx --- \n{}", e.getMessage());
            return null;
        }
    }

    private void updateProfilePicturePipeline(List<MultipartFile> files, UUID userId, String authorization, UUID oldId) {
        Set<UUID> updatedProfile = new HashSet<UUID>();
        try {
            updatedProfile = mediaService.uploadMedia(files, userId, authorization);
        } catch (Exception e) {
            logger.error("[PROFILEAGGREGATIONSERVICE updateProfilePicturePipeline] Could not upload file --- \n{}", e.getMessage());
            throw new RuntimeException("One of the responsible services offline");
        }

        String uuid = updatedProfile.stream().findFirst()
                .map(UUID::toString)
                .orElseThrow(() -> new RuntimeException("Uploaded file was banned on the Hobbiton platform"));

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.mediaPointer = uuid;

        if (oldId != null) {
            try {
                mediaClient.deleteMediaInstance(oldId);
            } catch (Exception e) {
                logger.error("[PROFILEAGGREGATIONSERVICE updateProfilePicturePipeline] Could not delete file --- \n{}", e.getMessage());
                throw new RuntimeException("One of the responsible services offline");
            }
        }

        try {
            profileClient.updateProfile(updateProfileDTO, authorization);
        } catch (Exception e) {
            logger.error("[PROFILEAGGREGATIONSERVICE updateProfilePicturePipeline] Could not update profile --- \n{}", e.getMessage());
            throw new RuntimeException("One of the responsible services offline");
        }
    }

    private List<AdminProfileSummaryResponse> finaliseProfiles(List<AdminProfileSummaryResponse> response) {
        List<MediaInstance> mediaPointers = response.stream()
                .filter(profile -> profile.profilePictureUrl != null && !profile.profilePictureUrl.isEmpty())
                .map(profile -> new MediaInstance(
                        UUID.fromString(profile.profilePictureUrl)
                ))
                .collect(Collectors.toList());
        if (!mediaPointers.isEmpty()) {
            try {
                List<MediaDTO> media = mediaService.getMedia(mediaPointers);
                Map<String, MediaDTO> mediaByRelevantId = media.stream()
                        .collect(Collectors.toMap(
                                m -> m.relevantId.toString(),
                                m -> m
                        ));

                // Enrich each profile with its matching MediaDTO
                response.forEach(profile -> {
                    if (profile.profilePictureUrl != null) {
                        profile.profilePicture = mediaByRelevantId.get(profile.profilePictureUrl);
                    }
                });
            } catch (RuntimeException e) {
                logger.error("[PROFILEAGGREGATIONSERVICE getAllProfiles] Could not send request to mediaServiceElx --- \n, {}", e.getMessage());
                response.forEach(profile -> {
                    if (profile.profilePictureUrl != null) {
                        profile.profilePicture = null;
                    }
                });
            }
        }

        return response;
    }
}
