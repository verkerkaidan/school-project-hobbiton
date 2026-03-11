package com.feedservice.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class UserFeed {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private UUID userId;

    public UserFeed() {
    }

    public UserFeed(UUID id, String description, String title, UUID userId) {
        this.id = id;
        this.description = description;
        this.title = title;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}


