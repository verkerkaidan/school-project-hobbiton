package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.interaction.CommentDTO;
import com.apigateway.apigateway.modelDtos.interaction.InteractionBatchResult;
import com.apigateway.apigateway.models.Interaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "interaction-service", url="${interaction.service.url}")
public interface InteractionClient {
    @PutMapping("/interaction/{user_id}/{reference_id}")
    boolean toggleLike(
            @PathVariable("user_id") UUID userId,
            @PathVariable("reference_id") UUID referenceId
    );

    @GetMapping("/interaction/{reference_id}")
    Interaction getInteraction(
            @PathVariable("reference_id") UUID referenceId
    );

    @GetMapping("/interaction/{user_id}/{reference_id}")
    boolean doesLikeExist(
            @PathVariable("user_id") UUID userId,
            @PathVariable("reference_id") UUID referenceId
    );

    @PostMapping("/interaction")
    UUID createComment(
            @RequestBody CommentDTO commentDTO
    );

    /** Bulk fetch interactions for a list of post IDs — 2 DB queries total, regardless of batch size. */
    @PostMapping("/interaction/batch")
    List<InteractionBatchResult> batchGetInteractions(@RequestBody List<UUID> referenceIds);

    /** Which of the given reference IDs has userId liked? Returns the liked subset — 1 DB query. */
    @PostMapping("/interaction/likes/batch")
    Set<UUID> batchDoesUserLike(@RequestParam("userId") UUID userId, @RequestBody List<UUID> referenceIds);
}
