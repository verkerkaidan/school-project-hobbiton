package com.apigateway.apigateway.modelDtos.interaction;

import com.apigateway.apigateway.models.Interaction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

/**
 * Gateway-side mirror of BatchInteractionDTO from the interaction-service.
 * Returned by POST /interaction/batch.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InteractionBatchResult {
    public UUID referenceId;
    public int likes;
    public List<Interaction.Comment> comments;

    public InteractionBatchResult() {}

    /** Convenience conversion to the Interaction model used by GraphQL resolvers. */
    public Interaction toInteraction() {
        return new Interaction(likes, comments != null ? comments : List.of());
    }
}
