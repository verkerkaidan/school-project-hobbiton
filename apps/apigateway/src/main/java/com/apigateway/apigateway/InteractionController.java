package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.InteractionClient;
import com.apigateway.apigateway.modelDtos.interaction.CommentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:19293"})
public class InteractionController {
    private final InteractionClient interactionClient;

    public InteractionController(InteractionClient interactionClient) {
        this.interactionClient = interactionClient;
    }

    @GetMapping("/interaction/{userId}/{referenceId}")
    public ResponseEntity<Boolean> doesLikeExist(
            @PathVariable UUID userId,
            @PathVariable UUID referenceId
    ) {
        boolean result = interactionClient.doesLikeExist(userId, referenceId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/interaction/{userId}/{referenceId}")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable UUID userId,
            @PathVariable UUID referenceId
    ) {
        boolean result = interactionClient.toggleLike(userId, referenceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/interaction")
    public ResponseEntity<UUID> createComment(
            @RequestBody CommentDTO comment
    ) {
        UUID result = interactionClient.createComment(comment);
        return ResponseEntity.ok(result);
    }
}
