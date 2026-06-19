package com.profileservice.profileservice.dto;

public class ProfileSummaryDTO {
    private final String userId;
    private final String username;
    private final String profilePictureUrl;

    public ProfileSummaryDTO(String userId, String username, String profilePictureUrl) {
        this.userId = userId;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
}