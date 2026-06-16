package com.apigateway.apigateway.models.other;

import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.media.MediaInstance;
import com.apigateway.apigateway.models.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A other post — no hobby-specific fields.
 * Useful when a user just wants to log something with linked items
 * (a book, movie, or exercise) without filling in GYM or READING details.
 */
public class Other extends Post {

    public Other() {}

    public Other(UUID id, String description, String title, UUID userId,
                 LocalDateTime createdAt, LocalDateTime updatedAt,
                 List<MediaInstance> mediaInstanceSet, String hobbyType,
                 Interaction interaction) {
        super(id, description, title, userId, createdAt, updatedAt,
                mediaInstanceSet, hobbyType, interaction);
    }
}