package com.profileservice.profileservice.dto;

public class ProfileSummaryDTO {
    private final String username;
    private final String profilePictureUrl;

    public ProfileSummaryDTO(String username, String profilePictureUrl) {
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
}