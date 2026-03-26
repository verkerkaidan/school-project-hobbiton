package com.profileconnectionservice.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import com.profileconnectionservice.model.AuditLog;
import com.profileconnectionservice.service.AuditLogService;
import com.profileconnectionservice.service.FollowService;

@Slf4j

@RestController
@RequestMapping("/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Toggle follow/unfollow relationship.
     * PUT /api/follows/{targetProfileId}
     * Header: X-Profile-Id (temporary - will be replaced with authentication)
     * 
     * If the relationship exists, it will be removed (unfollow).
     * If the relationship doesn't exist, it will be created (follow).
     * 
     * @return true if now following, false if now unfollowed
     */
    @PutMapping("/{targetProfileId}")
    public ResponseEntity<Boolean> toggleFollow(
            @PathVariable UUID targetProfileId,
            @RequestHeader("X-Profile-Id") UUID currentProfileId) {
        log.debug("Received PUT /follows/{} with currentProfileId={}", targetProfileId, currentProfileId);
        try {
            boolean isNowFollowing = followService.toggleFollow(currentProfileId,
                    targetProfileId);
            log.debug("toggleFollow completed: isNowFollowing={}", isNowFollowing);
            return ResponseEntity.ok(isNowFollowing);
        } catch (Exception e) {
            log.error("Error during toggleFollow: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all followers of a profile.
     * GET /api/follows/{profileId}/followers
     */
    @GetMapping("/{profileId}/followers")
    public ResponseEntity<Set<UUID>> getFollowers(@PathVariable UUID profileId) {
        log.debug("Received GET /follows/{}/followers", profileId);
        try {
            Set<UUID> followers = followService.getFollowers(profileId);
            log.debug("getFollowers returned {} followers", followers.size());
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            log.error("Error during getFollowers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all profiles that a profile is following.
     * GET /api/follows/{profileId}/following
     */
    @GetMapping("/{profileId}/following")
    public ResponseEntity<Set<UUID>> getFollowing(@PathVariable UUID profileId) {
        log.debug("Received GET /follows/{}/following", profileId);
        try {
            Set<UUID> following = followService.getFollowing(profileId);
            log.debug("getFollowing returned {} following profiles", following.size());
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            log.error("Error during getFollowing: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete all follow records for a user (GDPR compliance).
     * DELETE /api/follows/user/{userUUID}
     * 
     * Deletes all follow records where the user is either the initiator (follower)
     * or target (being followed).
     * Creates an audit log entry for compliance tracking.
     * 
     * @param userUUID the user whose follow records should be deleted
     * @return 200 OK with deletion summary containing count and audit log ID
     */
    @DeleteMapping("/user/{userUUID}")
    public ResponseEntity<Map<String, Object>> deleteUserData(@PathVariable UUID userUUID) {
        log.info("Received DELETE /follows/user/{} - initiating user data deletion", userUUID);
        try {
            int deletedCount = followService.deleteAllUserData(userUUID);

            // Retrieve the audit log entry for this deletion
            AuditLog auditLog = auditLogService.logDeletion(userUUID, "FOLLOW", deletedCount);

            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedCount);
            response.put("auditLogId", auditLog.getId());
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "Successfully deleted " + deletedCount + " follow records for user " + userUUID);

            log.info("Successfully deleted {} follow records for user: {}", deletedCount, userUUID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during delete user data for follows: {}", e.getMessage(), e);
            throw e;
        }
    }
}
