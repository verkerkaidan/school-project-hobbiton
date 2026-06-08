package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.models.AuthenticationRequest;
import com.apigateway.apigateway.models.AuthenticationResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service-c", url = "${auth.service.url}")
public interface AuthClient {
    @PostMapping("/auth/register")
    AuthenticationResponse createAuthentication(
        @RequestBody AuthenticationRequest request
    );

    @DeleteMapping("/auth/{userId}")
    void deleteAuthentication(@PathVariable UUID userId);

    @GetMapping("/auth/{userId}")
    AuthenticationResponse getAuthentication(@PathVariable UUID userId);
}
