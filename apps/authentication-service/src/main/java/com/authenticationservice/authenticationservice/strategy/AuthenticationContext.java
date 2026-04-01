package com.authenticationservice.authenticationservice.strategy;

import com.authenticationservice.authenticationservice.dto.AuthenticationRequest;
import com.authenticationservice.authenticationservice.dto.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The context in the Strategy Pattern.
 * Controllers call this — they declare WHAT they want (login/register + AuthType)
 * without knowing HOW it's implemented. Swapping or adding strategies
 * requires zero changes here or in the controllers.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationContext {

    private final AuthenticationStrategyFactory factory;

    public AuthenticationResponse authenticate(AuthType type, AuthenticationRequest request) {
        return factory.getStrategy(type).authenticate(request);
    }

    public AuthenticationResponse register(AuthType type, AuthenticationRequest request) {
        return factory.getStrategy(type).register(request);
    }
}
