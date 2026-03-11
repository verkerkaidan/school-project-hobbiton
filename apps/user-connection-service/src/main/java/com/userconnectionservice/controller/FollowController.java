package com.userconnectionservice.controller;

import com.userconnectionservice.service.FollowService;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

    /**
     * Toggle follow/unfollow relationship.
     * PUT /api/follows/{targetUserId}
     * Header: X-User-Id (temporary - will be replaced with authentication)
     * 
     * If the relationship exists, it will be removed (unfollow).
     * If the relationship doesn't exist, it will be created (follow).
     * 
     * @return true if now following, false if now unfollowed
     */
    @PutMapping("/{targetUserId}")
    public ResponseEntity<Boolean> toggleFollow(
            @PathVariable UUID targetUserId,
            @RequestHeader("X-User-Id") UUID currentUserId) {

        boolean isNowFollowing = followService.toggleFollow(currentUserId,
                targetUserId);
        return ResponseEntity.ok(isNowFollowing);
    }

    /**
     * Get all followers of a user.
     * GET /api/follows/{userId}/followers
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Set<UUID>> getFollowers(@PathVariable UUID userId) {
        Set<UUID> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Get all users that a user is following.
     * GET /api/follows/{userId}/following
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<Set<UUID>> getFollowing(@PathVariable UUID userId) {
        Set<UUID> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
}
