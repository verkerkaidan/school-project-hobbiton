package com.apigateway.apigateway.modelDtos;

import java.util.UUID;

public class UserProfile {
    public UUID id;
    public String username;
    public String bio;
    public String profilePictureUrl;
    public Integer followerCount;
    public Integer followingCount;

    public UserProfile(UUID id, String username, String bio, String profilePictureUrl, Integer followerCount, Integer followingCount) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}
