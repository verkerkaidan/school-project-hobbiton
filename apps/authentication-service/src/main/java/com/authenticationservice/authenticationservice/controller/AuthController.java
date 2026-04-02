package com.authenticationservice.authenticationservice.controller;

import com.authenticationservice.authenticationservice.dao.RoleRepository;
import com.authenticationservice.authenticationservice.dao.UserRepository;
import com.authenticationservice.authenticationservice.dto.AuthenticationRequest;
import com.authenticationservice.authenticationservice.dto.AuthenticationResponse;
import com.authenticationservice.authenticationservice.models.CustomUser;
import com.authenticationservice.authenticationservice.models.Role;
import com.authenticationservice.authenticationservice.strategy.AuthType;
import com.authenticationservice.authenticationservice.strategy.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:19293"})
@RequiredArgsConstructor
public class AuthController {

    // Auth operations delegate to the strategy context
    private final AuthenticationContext authContext;

    // Role management + delete still need direct repo access
    // (consider extracting to a UserManagementService in a future pass)
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authContext.register(AuthType.JWT, request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authContext.authenticate(AuthType.JWT, request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody AuthenticationRequest request) {
        Optional<CustomUser> userOptional = userRepository.findByEmail(request.email);
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid user provided");
        }
        CustomUser user = userOptional.get();
        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
        }
        userRepository.delete(user);
        return ResponseEntity.ok("Successfully removed user");
    }

    @PutMapping("/admin/new/{userId}")
    public ResponseEntity<String> adminFunctionality(@PathVariable String userId) {
        CustomUser user = findUserOrThrow(userId);
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        user.setRoles(Collections.singletonList(adminRole));
        userRepository.save(user);
        return ResponseEntity.ok("Successfully made user " + userId + " an Administrator");
    }

    @PutMapping("/staff/new/{userId}")
    public ResponseEntity<String> staffFunctionality(@PathVariable String userId) {
        CustomUser user = findUserOrThrow(userId);
        Role staffRole = roleRepository.findByName("ROLE_STAFF");
        user.setRoles(Collections.singletonList(staffRole));
        userRepository.save(user);
        return ResponseEntity.ok("Successfully made user " + userId + " a Staff member");
    }

    private CustomUser findUserOrThrow(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No valid user provided"));
    }
}
