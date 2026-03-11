package com.profileconnectionservice.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.profileconnectionservice.service.FollowService;

@RestController
@RequestMapping("/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

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

        boolean isNowFollowing = followService.toggleFollow(currentProfileId,
                targetProfileId);
        return ResponseEntity.ok(isNowFollowing);
    }

    /**
     * Get all followers of a profile.
     * GET /api/follows/{profileId}/followers
     */
    @GetMapping("/{profileId}/followers")
    public ResponseEntity<Set<UUID>> getFollowers(@PathVariable UUID profileId) {
        Set<UUID> followers = followService.getFollowers(profileId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Get all profiles that a profile is following.
     * GET /api/follows/{profileId}/following
     */
    @GetMapping("/{profileId}/following")
    public ResponseEntity<Set<UUID>> getFollowing(@PathVariable UUID profileId) {
        Set<UUID> following = followService.getFollowing(profileId);
        return ResponseEntity.ok(following);
    }
}
