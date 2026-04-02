package com.apigateway.apigateway.models;

import java.util.UUID;

public class UserProfile {
    private UUID id;
    private String username;
    private String bio;
    private String profilePictureUrl;
    private Integer followerCount;
    private Integer followingCount;

    public UserProfile() {
    }

    public UserProfile(UUID id, String username, String bio, String profilePictureUrl, Integer followerCount,
            Integer followingCount) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }
}
