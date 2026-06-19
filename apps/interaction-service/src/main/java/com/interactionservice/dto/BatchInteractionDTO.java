package com.interactionservice.dto;

import com.interactionservice.model.Comment;

import java.util.List;
import java.util.UUID;

/**
 * Returned by POST /interaction/batch.
 * Carries the full interaction (likes + comments) for one post/reference.
 */
public class BatchInteractionDTO {
    public UUID referenceId;
    public int likes;
    public List<Comment> comments;

    public BatchInteractionDTO() {}

    public BatchInteractionDTO(UUID referenceId, int likes, List<Comment> comments) {
        this.referenceId = referenceId;
        this.likes = likes;
        this.comments = comments;
    }
}
