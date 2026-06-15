package com.apigateway.apigateway.registration;

import com.apigateway.apigateway.models.RegistrationRequest;
import java.util.UUID;

public class RegistrationMessage {

    private UUID registrationId;

    private RegistrationRequest request;

    public RegistrationMessage() {
    }

    public RegistrationMessage(
            UUID registrationId,
            RegistrationRequest request) {

        this.registrationId = registrationId;
        this.request = request;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public RegistrationRequest getRequest() {
        return request;
    }
}