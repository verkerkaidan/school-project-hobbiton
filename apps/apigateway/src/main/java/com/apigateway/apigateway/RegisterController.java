package com.apigateway.apigateway;

import com.apigateway.apigateway.models.RegistrationRequest;
import com.apigateway.apigateway.models.RegistrationResponse;
import com.apigateway.apigateway.registration.RegistrationService;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final RegistrationService service;

    public RegisterController(
            RegistrationService service) {

        this.service = service;
    }
    @PostMapping
    public ResponseEntity<RegistrationResponse> register(
            @RequestBody RegistrationRequest request) {

        RegistrationResponse response =
                service.submitRegistration(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return switch (response.getErrorCode()) {
            case "BAD_REQUEST",
                 "CONFLICT" ->
                    ResponseEntity.badRequest()
                            .body(response);
            case "UNAUTHORIZED" ->
                    ResponseEntity.status(401)
                            .body(response);
            case "FORBIDDEN" ->
                    ResponseEntity.status(403)
                            .body(response);
            case "NOT_FOUND" ->
                    ResponseEntity.status(404)
                            .body(response);
            case "SERVICE_UNAVAILABLE" ->
                    ResponseEntity.status(503)
                            .body(response);
            case "GATEWAY_TIMEOUT" ->
                    ResponseEntity.status(504)
                            .body(response);
            default ->
                    ResponseEntity.status(500)
                            .body(response);
        };
    }
}
