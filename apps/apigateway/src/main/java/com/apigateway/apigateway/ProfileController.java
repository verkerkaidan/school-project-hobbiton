package com.apigateway.apigateway;

import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.modelDtos.profile.UpdateProfileDTO;
import com.apigateway.apigateway.models.AdminProfileSummaryResponse;
import com.apigateway.apigateway.models.media.MediaInstance;
import com.apigateway.apigateway.models.User;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.ProfileResponse;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.service.MediaService;
import com.apigateway.apigateway.service.ProfileAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileAggregationService profileAggregationService;
    private final MediaService mediaService;
    private final ProfileClient profileClient;


    public ProfileController(ProfileAggregationService profileAggregationService, MediaService mediaService, ProfileClient profileClient) {
        this.profileAggregationService = profileAggregationService;
        this.mediaService = mediaService;
        this.profileClient = profileClient;
    }

    @SchemaMapping(typeName = "User", field = "profilePicture")
    public MediaDTO mediaSet(User user) {
        if (user.getProfilePicture() == null || user.getProfilePicture().relevantId == null) {
            return null;
        }
        List<MediaInstance> dtos = new ArrayList<>();
        dtos.add(new MediaInstance(user.getProfilePicture().relevantId));
        MediaDTO dto = mediaService.getMedia(dtos).getFirst();

        return dto;
    }

    // GraphQL query: resolves a profile by username
    @QueryMapping
    public User getProfileByUsername(@Argument String username) {
        return profileAggregationService.getProfileByUsername(username);
    }

    // GraphQL query: resolves a single user's aggregated profile (bio, picture,
    // counts, etc.)
    @QueryMapping
    public User getProfile(@Argument String userId) {
        logger.info("[getProfile] Received request for userId: {}", userId);
        try {
            User profile = profileAggregationService.getProfile(UUID.fromString(userId));
            logger.info("[getProfile] Successfully retrieved profile for userId: {} with pfp: {}", userId, profile.getProfilePicture() != null ? profile.getProfilePicture().relevantId : "Not set yet");
            return profile;
        } catch (Exception e) {
            logger.error("[getProfile] Error retrieving profile for userId: {}", userId, e);
            throw e;
        }
    }

    // GraphQL query: returns the list of users who follow the given userId
    @QueryMapping
    public List<User> getFollowers(@Argument String userId) {
        logger.info("[getFollowers] Received request for userId: {}", userId);
        try {
            List<User> followers = profileAggregationService.getFollowers(UUID.fromString(userId));
            logger.info("[getFollowers] Retrieved {} followers for userId: {}", followers.size(), userId);
            return followers;
        } catch (Exception e) {
            logger.error("[getFollowers] Error retrieving followers for userId: {}", userId, e);
            throw e;
        }
    }

    // GraphQL query: returns the list of users that the given userId follows
    @QueryMapping
    public List<User> getFollowing(@Argument String userId) {
        logger.info("[getFollowing] Received request for userId: {}", userId);
        try {
            List<User> following = profileAggregationService.getFollowing(UUID.fromString(userId));
            logger.info("[getFollowing] Retrieved {} following for userId: {}", following.size(), userId);
            return following;
        } catch (Exception e) {
            logger.error("[getFollowing] Error retrieving following for userId: {}", userId, e);
            throw e;
        }
    }

    // GraphQL query: lightweight relationship check — does currentUser follow
    // targetUser?
    @QueryMapping
    public Boolean isFollowing(@Argument String currentUserId, @Argument String targetUserId) {
        return profileAggregationService.isFollowing(
                UUID.fromString(currentUserId), UUID.fromString(targetUserId));
    }

    // GraphQL query: returns the list of users that the given userId has blocked
    @QueryMapping
    public List<User> getBlockedUsers(@Argument String userId) {
        logger.info("[getBlockedUsers] Received request for userId: {}", userId);
        try {
            List<User> blockedUsers = profileAggregationService.getBlockedUsers(UUID.fromString(userId));
            logger.info("[getBlockedUsers] Retrieved {} blocked users for userId: {}", blockedUsers.size(), userId);
            return blockedUsers;
        } catch (Exception e) {
            logger.error("[getBlockedUsers] Error retrieving blocked users for userId: {}", userId, e);
            throw e;
        }
    }

    // GraphQL query: checks whether currentUser has blocked targetUser
    @QueryMapping
    public Boolean isBlocking(@Argument String currentUserId, @Argument String targetUserId) {
        return profileAggregationService.isBlocking(
                UUID.fromString(currentUserId), UUID.fromString(targetUserId));
    }

    // GraphQL mutation: follow or unfollow targetUser; returns true if now
    // following, false if unfollowed
    @MutationMapping
    public Boolean toggleFollow(@Argument String targetUserId, @Argument String currentUserId) {
        logger.info("[toggleFollow] currentUserId={} targetUserId={}", currentUserId, targetUserId);
        try {
            Boolean result = profileAggregationService.toggleFollow(
                    UUID.fromString(currentUserId), UUID.fromString(targetUserId));
            logger.info("[toggleFollow] Result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("[toggleFollow] Error for currentUserId={}, targetUserId={}", currentUserId, targetUserId, e);
            throw e;
        }
    }

    // GraphQL mutation: block or unblock targetUser; returns true if now blocked,
    // false if unblocked
    @MutationMapping
    public Boolean toggleBlock(@Argument String targetUserId, @Argument String currentUserId) {
        logger.info("[toggleBlock] currentUserId={} targetUserId={}", currentUserId, targetUserId);
        try {
            Boolean result = profileAggregationService.toggleBlock(
                    UUID.fromString(currentUserId), UUID.fromString(targetUserId));
            logger.info("[toggleBlock] Result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("[toggleBlock] Error for currentUserId={}, targetUserId={}", currentUserId, targetUserId, e);
            throw e;
        }
    }

    // GraphQL mutation: submits an abuse report against a user; always returns true
    // on success
    @MutationMapping
    public Boolean createReport(@Argument String initiatorId, @Argument String targetId,
            @Argument String reason, @Argument String description) {
        logger.info("[createReport] initiatorId={} targetId={} reason={}", initiatorId, targetId, reason);
        try {
            profileAggregationService.createReport(
                    UUID.fromString(initiatorId), UUID.fromString(targetId), reason, description);
            return true;
        } catch (Exception e) {
            logger.error("[createReport] Error for initiatorId={}, targetId={}", initiatorId, targetId, e);
            throw e;
        }
    }

    // GraphQL query: fetches all posts authored by the given userId (used on
    // profile pages)
    @QueryMapping
    public List<Post> getUserPosts(@Argument String userId) {
        logger.info("[getUserPosts] Received request for userId: {}", userId);
        try {
            List<Post> posts = profileAggregationService.getUserPosts(UUID.fromString(userId));
            logger.info("[getUserPosts] Retrieved {} posts for userId: {}", posts.size(), userId);
            return posts;
        } catch (Exception e) {
            logger.error("[getUserPosts] Error retrieving posts for userId: {}", userId, e);
            throw e;
        }
    }

    // REST endpoint to update the authenticated user's profile fields
    @ResponseBody
    @PutMapping("/profile/update")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestBody UpdateProfileDTO update,
            @RequestHeader("Authorization") String authorization
    ) {
        ProfileResponse profile = profileClient.updateProfile(update, authorization);
        return ResponseEntity.ok(profile);
    }

    // Handles the changing of a users profile picture
    @ResponseBody
    @PostMapping(value = "/change-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPost(
            @RequestParam("userId") UUID userId,
            @RequestPart(value = "files[]") List<MultipartFile> files,
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "oldId", required = false) UUID oldId
    ) {
        profileAggregationService.updateProfilePicture(userId, files, authorization, oldId);
        return ResponseEntity.ok("Profile picture was updated");
    }

    @ResponseBody
    @GetMapping(value = "/search-profiles/{query}")
    public List<AdminProfileSummaryResponse> fuzzySearchProfilesByUsername(@PathVariable String query) {
        return profileAggregationService.fuzzySearchProfilesByUsername(query);
    }
}
