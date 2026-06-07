package com.apigateway.apigateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class AuthenticationResponse {

    private UUID userId;
    private String email;

    @JsonProperty("token")
    private String accessToken;

    private String refreshToken;

    public AuthenticationResponse() {}

    public AuthenticationResponse(
        UUID userId,
        String email,
        String accessToken,
        String refreshToken
    ) {
        this.userId = userId;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
