package com.interactionservice.dto;

import java.util.UUID;

public class CommentDTO {
    public UUID userId;
    public UUID referenceId;
    public String comment;

    public CommentDTO(UUID userId, UUID referenceId, String comment) {
        this.userId = userId;
        this.referenceId = referenceId;
        this.comment = comment;
    }

    public CommentDTO() {
    }
}
