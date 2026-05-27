package com.apigateway.apigateway.modelDtos.interaction;

import java.util.UUID;

public class CommentDTO {
    public UUID referenceId;
    public UUID userId;
    public String comment;

    public CommentDTO(UUID referenceId, UUID userId, String comment) {
        this.referenceId = referenceId;
        this.userId = userId;
        this.comment = comment;
    }
}
