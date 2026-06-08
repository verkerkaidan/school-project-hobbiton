package com.apigateway.apigateway.models.other;

import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.MediaInstance;
import com.apigateway.apigateway.models.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Other extends Post {

    public Other() {
        super();
    }

    public Other(UUID id, String description, String title, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt, List<MediaInstance> mediaInstanceSet, String hobbyType, Interaction interaction) {
        super(id, description, title, userId, createdAt, updatedAt, mediaInstanceSet, hobbyType, interaction);
    }
}
