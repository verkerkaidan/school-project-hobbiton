package com.interactionservice.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "custom_like")
public class CustomLike {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "reference_id")
    private UUID referenceId;
    @Column(name = "user_id")
    private UUID userId;

    public CustomLike(UUID referenceId, UUID userId) {
        this.referenceId = referenceId;
        this.userId = userId;
    }

    public CustomLike() {
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID user_id) {
        this.userId = user_id;
    }
}
