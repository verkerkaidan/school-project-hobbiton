package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.modelDtos.CreateProfileRequest;
import com.apigateway.apigateway.models.ProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// REST controller for profile management operations that require forwarding the
// caller's JWT (e.g. creation, where the profile-service derives userId from the token)
@RestController
public class ProfileManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileManagementController.class);

    private final ProfileClient profileClient;

    public ProfileManagementController(ProfileClient profileClient) {
        this.profileClient = profileClient;
    }

    // Creates a new profile by forwarding the request body and Bearer token to the
    // profile-service. The profile-service extracts the userId from the JWT claims.
    @PostMapping("/profile/create")
    public ResponseEntity<ProfileResponse> createProfile(
            @RequestBody CreateProfileRequest request,
            @RequestHeader("Authorization") String authorization) {
        logger.info("[createProfile] Creating profile for username: {}", request.username);
        try {
            ProfileResponse profile = profileClient.createProfile(request, authorization);
            logger.info("[createProfile] Profile created successfully for username: {}", request.username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("[createProfile] Failed to create profile for username: {} - {}", request.username,
                    e.getMessage(), e);
            throw e;
        }
    }
}
