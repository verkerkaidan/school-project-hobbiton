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
import com.profileconnectionservice.service.BlockService;

@Slf4j

@RestController
@RequestMapping("/blocks")
public class BlockController {

    @Autowired
    private BlockService blockService;

    @Autowired
    private AuditLogService auditLogService;

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
        log.debug("Received PUT /blocks/{} with currentProfileId={}", targetProfileId, currentProfileId);
        try {
            boolean isNowBlocking = blockService.toggleBlock(currentProfileId,
                    targetProfileId);
            log.debug("toggleBlock completed: isNowBlocking={}", isNowBlocking);
            return ResponseEntity.ok(isNowBlocking);
        } catch (Exception e) {
            log.error("Error during toggleBlock: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all profiles that this profile has blocked.
     * GET /api/blocks/{profileId}/blocked
     */
    @GetMapping("/{profileId}/blocked")
    public ResponseEntity<Set<UUID>> getBlocked(@PathVariable UUID profileId) {
        log.debug("Received GET /blocks/{}/blocked", profileId);
        try {
            Set<UUID> blocked = blockService.getBlocked(profileId);
            log.debug("getBlocked returned {} blocked profiles", blocked.size());
            return ResponseEntity.ok(blocked);
        } catch (Exception e) {
            log.error("Error during getBlocked: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete all block records for a user (GDPR compliance).
     * DELETE /api/blocks/user/{userUUID}
     * 
     * Deletes all block records where the user is either the initiator (blocker) or
     * target (blocked).
     * Creates an audit log entry for compliance tracking.
     * 
     * @param userUUID the user whose block records should be deleted
     * @return 200 OK with deletion summary containing count and audit log ID
     */
    @DeleteMapping("/user/{userUUID}")
    public ResponseEntity<Map<String, Object>> deleteUserData(@PathVariable UUID userUUID) {
        log.info("Received DELETE /blocks/user/{} - initiating user data deletion", userUUID);
        try {
            int deletedCount = blockService.deleteAllUserData(userUUID);

            // Retrieve the most recent audit log entry for this user and action
            AuditLog auditLog = auditLogService.logDeletion(userUUID, "BLOCK", deletedCount);

            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedCount);
            response.put("auditLogId", auditLog.getId());
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "Successfully deleted " + deletedCount + " block records for user " + userUUID);

            log.info("Successfully deleted {} block records for user: {}", deletedCount, userUUID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during delete user data for blocks: {}", e.getMessage(), e);
            throw e;
        }
    }
}
