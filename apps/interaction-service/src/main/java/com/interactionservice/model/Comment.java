package com.interactionservice.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "comment"
)
public class Comment {
    @GeneratedValue
    @Id
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(name = "reference_id")
    private UUID referenceId;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "comment")
    private String comment;

    public Comment(UUID referenceId, UUID userId, String comment) {
        this.referenceId = referenceId;
        this.userId = userId;
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

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID reference_id) {
        this.referenceId = reference_id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID user_id) {
        this.userId = user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
