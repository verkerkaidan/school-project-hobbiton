package com.apigateway.apigateway;

import com.apigateway.apigateway.modelDtos.interaction.CommentDTO;
import com.apigateway.apigateway.service.InteractionAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
public class InteractionController {
    private final InteractionAggregationService interactionAggregationService;

    public InteractionController(InteractionAggregationService interactionAggregationService) {
        this.interactionAggregationService = interactionAggregationService;
    }

    @GetMapping("/interaction/{userId}/{referenceId}")
    public ResponseEntity<Boolean> doesLikeExist(
            @PathVariable UUID userId,
            @PathVariable UUID referenceId) {
        boolean result = interactionAggregationService.doesLikeExist(userId, referenceId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/interaction/{userId}/{referenceId}")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable UUID userId,
            @PathVariable UUID referenceId) {
        boolean result = interactionAggregationService.toggleLike(userId, referenceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/interaction")
    public ResponseEntity<UUID> createComment(
            @RequestBody CommentDTO comment) {
        UUID result = interactionAggregationService.createComment(comment);
        return ResponseEntity.ok(result);
    }

    /**
     * Batch like-state check. Returns the subset of referenceIds that userId has liked.
     * Replaces N individual GET /interaction/{userId}/{referenceId} calls with one request.
     */
    @PostMapping("/interaction/likes/batch")
    public ResponseEntity<Set<UUID>> batchLikeStatus(
            @RequestParam UUID userId,
            @RequestBody List<UUID> referenceIds) {
        Set<UUID> liked = interactionAggregationService.batchDoesUserLike(userId, referenceIds);
        return ResponseEntity.ok(liked);
    }
}
