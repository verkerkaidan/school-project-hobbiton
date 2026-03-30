package com.interactionservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Comment {
    @GeneratedValue
    @Id
    private UUID id;

    private UUID reference_id;
    private UUID user_id;
    private String comment;

    public Comment(UUID reference_id, UUID user_id, String comment) {
        this.reference_id = reference_id;
        this.user_id = user_id;
        this.comment = comment;
    }

    public Comment() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReference_id() {
        return reference_id;
    }

    public void setReference_id(UUID reference_id) {
        this.reference_id = reference_id;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
