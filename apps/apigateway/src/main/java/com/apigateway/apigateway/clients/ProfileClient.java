package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.profile.UpdateProfileDTO;
import com.apigateway.apigateway.models.CreateProfileRequest;
import com.apigateway.apigateway.models.AdminProfileSummaryResponse;
import com.apigateway.apigateway.models.ProfileResponse;
import com.apigateway.apigateway.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // Fetches all profiles with only username and profile picture URL.
    @GetMapping("/profile/all")
    List<AdminProfileSummaryResponse> getAllProfiles();

    // Bulk-fetch lightweight profile summaries for a specific set of user IDs.
    // Used by the gateway to enrich report data with usernames in a single request.
    @PostMapping("/profile/batch")
    List<AdminProfileSummaryResponse> getProfilesByIds(@RequestBody List<UUID> userIds);

    // Creates a new profile in the profile-service; the Bearer token is forwarded
    // so the
    // profile-service can extract the userId from the JWT claims
    @PostMapping("/profile/create")
    ProfileResponse createProfile(@RequestBody CreateProfileRequest request,
            @RequestHeader("Authorization") String authorization
    );

    @PutMapping("/profile/update")
    ProfileResponse updateProfile(
            @RequestBody UpdateProfileDTO update,
            @RequestHeader("Authorization") String authorization
    );
}
