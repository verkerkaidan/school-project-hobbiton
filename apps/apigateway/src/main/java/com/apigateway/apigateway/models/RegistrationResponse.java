package com.apigateway.apigateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class RegistrationResponse {

    private ProfileResponse profile;

    // The authentication token returned by the auth-service
    private String token;

    // The userId assigned by the auth-service
    @JsonProperty("userId")
    private UUID userId;

    private boolean success = true;
    private String errorCode;
    private String errorMessage;

    public RegistrationResponse() {}

    public RegistrationResponse(ProfileResponse profile, String token, UUID userId) {
        this.profile = profile;
        this.token = token;
        this.userId = userId;
    }

    public ProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(ProfileResponse profile) {
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
