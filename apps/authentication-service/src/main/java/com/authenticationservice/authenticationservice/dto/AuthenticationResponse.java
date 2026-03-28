package com.authenticationservice.authenticationservice.dto;

import java.util.UUID;

public class AuthenticationResponse {
    public UUID userId;
    public String token;

    public AuthenticationResponse(UUID userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
