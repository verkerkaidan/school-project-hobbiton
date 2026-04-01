package com.authenticationservice.authenticationservice.strategy.impl;

import com.authenticationservice.authenticationservice.config.JWTUtil;
import com.authenticationservice.authenticationservice.dao.RoleRepository;
import com.authenticationservice.authenticationservice.dao.UserRepository;
import com.authenticationservice.authenticationservice.dto.AuthenticationRequest;
import com.authenticationservice.authenticationservice.dto.AuthenticationResponse;
import com.authenticationservice.authenticationservice.models.CustomUser;
import com.authenticationservice.authenticationservice.models.Role;
import com.authenticationservice.authenticationservice.services.CredentialValidator;
import com.authenticationservice.authenticationservice.strategy.AuthType;
import com.authenticationservice.authenticationservice.strategy.AuthenticationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final CredentialValidator validator;

    @Override
    public AuthType getSupportedType() {
        return AuthType.JWT;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            Optional<CustomUser> customUserOptional = userRepository.findByEmail(request.email);
            if (customUserOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No valid credentials");
            }

            CustomUser customUser = customUserOptional.get();

            // Auth is keyed on UUID (not email) — matching your existing UserDetailsService
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(customUser.getId(), request.password)
            );

            String token = jwtUtil.generateToken(String.valueOf(customUser.getId()));
            return new AuthenticationResponse(customUser.getId(), token);

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No valid credentials");
        }
    }

    @Override
    public AuthenticationResponse register(AuthenticationRequest request) {
        if (!validator.isValidEmail(request.email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid email provided");
        }
        if (!validator.isValidPassword(request.password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid password provided");
        }
        if (userRepository.findByEmail(request.email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't register with this e-mail");
        }

        String encodedPassword = passwordEncoder.encode(request.password);
        List<Role> role = Collections.singletonList(roleRepository.findByName("ROLE_USER"));

        CustomUser registeredUser = new CustomUser(request.email, encodedPassword, role);
        userRepository.save(registeredUser);

        String token = jwtUtil.generateToken(String.valueOf(registeredUser.getId()));
        return new AuthenticationResponse(registeredUser.getId(), token);
    }
}
