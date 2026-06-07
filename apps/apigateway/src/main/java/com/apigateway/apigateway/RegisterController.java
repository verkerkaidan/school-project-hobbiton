package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.AuthClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.models.AuthenticationResponse;
import com.apigateway.apigateway.models.ProfileResponse;
import com.apigateway.apigateway.models.RegistrationRequest;
import com.apigateway.apigateway.models.RegistrationResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(
        RegisterController.class
    );

    private final ProfileClient profileClient;
    private final AuthClient authClient;

    public RegisterController(
        ProfileClient profileClient,
        AuthClient authClient
    ) {
        this.profileClient = profileClient;
        this.authClient = authClient;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(
        @RequestBody RegistrationRequest request
    ) {
        AuthenticationResponse auth = null;

        try {
            // Create authentication (auth service) first
            auth = createAuthentication(request);

            // Then create profile in profile-service using the returned access token
            ProfileResponse profile = createProfileInProfileService(
                request,
                auth.getAccessToken()
            );

            // Build a response containing the created profile and auth token
            RegistrationResponse response = new RegistrationResponse();
            response.setProfile(profile);
            response.setToken(auth.getAccessToken());
            response.setUserId(auth.getUserId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Rollback: if creating the profile failed after creating auth, delete the auth user
            if (auth != null) {
                deleteAuthentication(auth.getUserId());
            }

            throw e;
        }
    }

    private AuthenticationResponse createAuthentication(
        RegistrationRequest request
    ) {
        return authClient.createAuthentication(
            request.toAuthenticationRequest()
        );
    }

    private ProfileResponse createProfileInProfileService(
        RegistrationRequest request,
        String accessToken
    ) {
        return profileClient.createProfile(
            request.toProfileRequest(),
            "Bearer " + accessToken
        );
    }

    private void deleteAuthentication(UUID userId) {
        try {
            authClient.deleteAuthentication(userId);

            logger.warn("[rollback] Deleted auth user {}", userId);
        } catch (Exception e) {
            logger.error("[rollback] Failed to delete auth user {}", userId, e);
        }
    }
}
