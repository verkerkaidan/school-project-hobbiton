package com.apigateway.apigateway.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkedHobbyItem {
    private UUID id;
    private String externalId;
    private String hobbyType;       // "BOOK" | "MOVIE" | "EXERCISE"
    private String displayName;
    private String imageUrl;

    public LinkedHobbyItem() {}

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getHobbyType() { return hobbyType; }
    public void setHobbyType(String hobbyType) { this.hobbyType = hobbyType; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getHobbyId() { return id; }

}