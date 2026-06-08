package com.apigateway.apigateway.models;

import com.apigateway.apigateway.modelDtos.media.MediaDTO;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String bio;
    private Integer followerCount;
    private Integer followingCount;
    private MediaDTO profilePicture;

    public User() {
    }

    public User(UUID id, String username, MediaDTO profilePicture) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public User(UUID id, String username, String bio, MediaDTO profilePicture, Integer followerCount,
            Integer followingCount) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.profilePicture = profilePicture;
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

    public MediaDTO getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(MediaDTO profilePicture) {
        this.profilePicture = profilePicture;
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
