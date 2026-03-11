package com.profileconnectionservice.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.profileconnectionservice.service.BlockService;

@RestController
@RequestMapping("/blocks")
public class BlockController {

    @Autowired
    private BlockService blockService;

    /**
     * Toggle block/unblock relationship.
     * PUT /api/blocks/{targetProfileId}
     * Header: X-Profile-Id (temporary - will be replaced with authentication)
     * 
     * If the relationship exists, it will be removed (unblock).
     * If the relationship doesn't exist, it will be created (block).
     * 
     * @return true if now blocking, false if now unblocked
     */
    @PutMapping("/{targetProfileId}")
    public ResponseEntity<Boolean> toggleBlock(
            @PathVariable UUID targetProfileId,
            @RequestHeader("X-Profile-Id") UUID currentProfileId) {

        boolean isNowBlocking = blockService.toggleBlock(currentProfileId,
                targetProfileId);
        return ResponseEntity.ok(isNowBlocking);
    }

    /**
     * Get all profiles that this profile has blocked.
     * GET /api/blocks/{profileId}/blocked
     */
    @GetMapping("/{profileId}/blocked")
    public ResponseEntity<Set<UUID>> getBlocked(@PathVariable UUID profileId) {
        Set<UUID> blocked = blockService.getBlocked(profileId);
        return ResponseEntity.ok(blocked);
    }
}
