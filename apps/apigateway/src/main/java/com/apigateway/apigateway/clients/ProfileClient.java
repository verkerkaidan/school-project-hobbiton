package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.CreateProfileRequest;
import com.apigateway.apigateway.models.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

// Feign client for the profile-service; handles profile data (username, bio, picture, counts)
// The service URL is resolved from application properties at startup
@FeignClient(name = "profile-service", url = "${profile.service.url}")
public interface ProfileClient {
    // Fetches the full profile for a given userId from the profile-service
    @GetMapping("/profile/{userId}")
    ProfileResponse getProfile(@PathVariable UUID userId);

    // Fetches the full profile for a given username from the profile-service
    @GetMapping("/profile/by-username/{username}")
    ProfileResponse getProfileByUsername(@PathVariable String username);

    // Creates a new profile in the profile-service; the Bearer token is forwarded
    // so the
    // profile-service can extract the userId from the JWT claims
    @PostMapping("/profile/create")
    ProfileResponse createProfile(@RequestBody CreateProfileRequest request,
            @RequestHeader("Authorization") String authorization);
}
