package com.authenticationservice.authenticationservice.dto;

public class AuthenticationResponse {
    public String userId;
    public String token;

    public AuthenticationResponse(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
