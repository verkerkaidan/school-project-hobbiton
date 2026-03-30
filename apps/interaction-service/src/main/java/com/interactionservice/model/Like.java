package com.interactionservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Like {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID referenceId;
    private UUID user_id;

    public Like(UUID referenceId, UUID user_id) {
        this.referenceId = referenceId;
        this.user_id = user_id;
    }

    public Like() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }
}
