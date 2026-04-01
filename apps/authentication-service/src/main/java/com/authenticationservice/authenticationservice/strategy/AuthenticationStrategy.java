package com.authenticationservice.authenticationservice.strategy;

import com.authenticationservice.authenticationservice.dto.AuthenticationRequest;
import com.authenticationservice.authenticationservice.dto.AuthenticationResponse;

public interface AuthenticationStrategy {

    AuthType getSupportedType();

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse register(AuthenticationRequest request);
}
