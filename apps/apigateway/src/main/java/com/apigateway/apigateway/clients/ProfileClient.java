package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.models.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// Feign client for the profile-service; handles profile data (username, bio, picture, counts)
// The service URL is resolved from application properties at startup
@FeignClient(name = "profile-service", url = "${profile.service.url}")
public interface ProfileClient {
    // Fetches the full profile for a given userId from the profile-service
    @GetMapping("/profile/{userId}")
    ProfileResponse getProfile(@PathVariable UUID userId);
}
