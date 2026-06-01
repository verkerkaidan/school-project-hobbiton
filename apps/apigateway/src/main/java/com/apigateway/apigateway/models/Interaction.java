package com.apigateway.apigateway.models;

import java.util.List;
import java.util.UUID;

public class Interaction {
    public int likes;
    public List<Comment> comments;

    public Interaction(int likes, List<Comment> comments) {
        this.likes = likes;
        this.comments = comments;
    }

    public Interaction() {
    }

    public static class Comment {
        public UUID id;
        public UUID referenceId;
        public UUID userId;
        public String comment;

        public Comment() {
        }

        public Comment(UUID id, UUID referenceId, UUID userId, String comment) {
            this.id = id;
            this.referenceId = referenceId;
            this.userId = userId;
            this.comment = comment;
        }
    }
}
