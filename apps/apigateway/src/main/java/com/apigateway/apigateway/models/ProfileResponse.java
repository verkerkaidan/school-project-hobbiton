package com.apigateway.apigateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ProfileResponse {
    @JsonProperty("userId")
    private UUID userId;

    private String username;
    private String name;
    private String biography;
    private String mediaPointer;
    private String link;
    private String location;
    private OffsetDateTime lastEdited;
    private OffsetDateTime created_at;

    public ProfileResponse() {
    }

    // Getters
    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getBiography() {
        return biography;
    }

    public String getMediaPointer() {
        return mediaPointer;
    }

    public String getLink() {
        return link;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getLastEdited() {
        return lastEdited;
    }

    public OffsetDateTime getCreated_at() {
        return created_at;
    }
}
