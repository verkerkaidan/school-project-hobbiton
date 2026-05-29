package com.apigateway.apigateway.modelDtos;

import java.util.UUID;

public class UserProfile {
    public UUID id;
    public String username;
    public String bio;
    public String profilePictureUrl;
    public Integer followerCount;
    public Integer followingCount;
}
