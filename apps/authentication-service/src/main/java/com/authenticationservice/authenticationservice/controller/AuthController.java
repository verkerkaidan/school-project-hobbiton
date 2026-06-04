package com.authenticationservice.authenticationservice.controller;

import com.authenticationservice.authenticationservice.config.JWTUtil;
import com.authenticationservice.authenticationservice.dao.RoleRepository;
import com.authenticationservice.authenticationservice.dao.UserRepository;
import com.authenticationservice.authenticationservice.dto.AuthenticationRequest;
import com.authenticationservice.authenticationservice.dto.AuthenticationResponse;
import com.authenticationservice.authenticationservice.models.CustomUser;
import com.authenticationservice.authenticationservice.models.Role;
import com.authenticationservice.authenticationservice.services.CredentialValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(originPatterns = { "http://localhost:*",
                "http://*.student.inf.st.hsleiden.nl:*" }, allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final JWTUtil jwtUtil;
        private final AuthenticationManager authManager;
        private final PasswordEncoder passwordEncoder;
        private final CredentialValidator validator;

        /// This endpoint ("/api/auth/register") the user can register supplying their
        /// - Email
        /// - Password (hashed on the front-end?)
        /// If the /register body also has their other information (username, etc.) this
        /// request should be passed on to the profile service
        /// This endpoint will return to the user their userId & JWT token in an
        /// LoginResponse
        @PostMapping("/register")
        public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest registerRequest) {

                if (!validator.isValidEmail(registerRequest.email)) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "No valid email provided");
                }

                // an invalid password could also return a more specific error message?
                if (!validator.isValidPassword(registerRequest.password)) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "No valid password provided");
                }

                Optional<CustomUser> user = userRepository.findByEmail(registerRequest.email);

                if (user.isPresent()) {
                        // A user already exists - Nice to have: Send an email to create a user/ if the
                        // account already exists
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "Can't register with this e-mail");
                }

                String encodedPassword = passwordEncoder.encode(registerRequest.password);

                List<Role> role = Collections.singletonList(roleRepository.findByName("ROLE_USER"));

                CustomUser registeredUser = new CustomUser(registerRequest.email, encodedPassword, role);

                userRepository.save(registeredUser);
                Vector<String> roles = registeredUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toCollection(Vector::new));
                String token = jwtUtil.generateToken(String.valueOf(registeredUser.getId()), roles);
                AuthenticationResponse authResponse = new AuthenticationResponse(registeredUser.getId(), token);
                return ResponseEntity.ok(authResponse);
        }

        /// This endpoint ("/api/auth/login") will enable a user to authenticate with
        /// sending the following information
        /// - email
        /// - password
        /// and returns a userId and a JWT-token
        @PostMapping("/login")
        public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest body) {
                try {
                        Optional<CustomUser> customUserOptional = userRepository.findByEmail(body.email);
                        if (customUserOptional.isEmpty()) {
                                throw new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "No valid credentials");
                        }
                        CustomUser customUser = customUserOptional.get();

                        UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(
                                        customUser.getId(), body.password);

                        authManager.authenticate(authInputToken);

                        Vector<String> roles = customUser.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toCollection(Vector::new));
                        String token = jwtUtil.generateToken(String.valueOf(customUser.getId()), roles);
                        AuthenticationResponse loginResponse = new AuthenticationResponse(customUser.getId(), token);

                        return ResponseEntity.ok(loginResponse);

                } catch (AuthenticationException authExc) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN, "No valid credentials");
                }
        }

        /// This endpoint("api/auth/delete") will delete a users account
        /// to succeed the user will need to be authenticated
        /// the assumption is made that all other's data
        @DeleteMapping("/delete")
        public ResponseEntity<String> delete(@RequestBody AuthenticationRequest authenticationRequest) {
                Optional<CustomUser> userOptional = userRepository.findByEmail(authenticationRequest.email);
                if (userOptional.isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "No valid user provided");
                }
                CustomUser user = userOptional.get();
                if (!passwordEncoder.matches(authenticationRequest.password, user.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
                }
                userRepository.delete(user);

                return ResponseEntity.ok("Succesfully removed user");
        }

        @PutMapping("/admin/new/{userId}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<String> adminFunctionality(
                @RequestBody AuthenticationRequest authenticationRequest,
                @PathVariable String userId) {

                // Validate UUID format before hitting the DB
                UUID targetUserId;
                try {
                        targetUserId = UUID.fromString(userId);
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID format");
                }


                // validate user
                Optional<CustomUser> authUser = userRepository.findByEmail(authenticationRequest.email);
                if (authUser.isEmpty()){
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user authenticated");
                }

                CustomUser currentUser = authUser.get();
                // Prevent self-role modification (optional but recommended)
                if (currentUser.getId().equals(UUID.fromString(userId))) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify your own roles");
                }

                CustomUser user = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

                Role adminRole = Optional.ofNullable(roleRepository.findByName("ROLE_ADMIN"))
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Admin role not configured"));

                user.setRoles(new ArrayList<>(Collections.singletonList(adminRole)));
                userRepository.save(user);

                log.info("Admin '{}' granted ROLE_ADMIN to user '{}'", currentUser.getId(), userId);

                return ResponseEntity.ok("Successfully made user " + userId + " an Administrator");
        }

        @PutMapping("/staff/new/{userId}")
        public ResponseEntity<String> staffFunctionality(@PathVariable String userId) {
                Optional<CustomUser> userOptional = userRepository.findById(UUID.fromString(userId));
                if (userOptional.isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "No valid user provided");
                }
                CustomUser user = userOptional.get();
                Role staffRole = roleRepository.findByName("ROLE_STAFF");
                user.setRoles(Collections.singletonList(staffRole));
                userRepository.save(user);
                return ResponseEntity.ok("Successfully made user " + userId + " a Staff member");
        }
}
