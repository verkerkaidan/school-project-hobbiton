package com.apigateway.apigateway;

import com.apigateway.apigateway.models.User;
import com.apigateway.apigateway.models.UserProfile;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.service.ProfileAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileAggregationService profileAggregationService;

    public ProfileController(ProfileAggregationService profileAggregationService) {
        this.profileAggregationService = profileAggregationService;
    }

    // GraphQL query: resolves a single user's aggregated profile (bio, picture,
    // counts, etc.)
    @QueryMapping
    public UserProfile getProfile(@Argument String userId) {
        logger.info("[getProfile] Received request for userId: {}", userId);
        try {
            UserProfile profile = profileAggregationService.getProfile(UUID.fromString(userId));
            logger.info("[getProfile] Successfully retrieved profile for userId: {}", userId);
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
}
