package com.apigateway.apigateway.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Simple Feign client to forward requests to the library-service (Rust).
 * Methods use other Object/Map types to avoid copying the Rust DTOs.
 */
@FeignClient(name = "library-service", url = "${library.service.url}")
public interface LibraryClient {

    @PostMapping(path = "/api/library/search", consumes = "application/json")
    Object searchPost(@RequestHeader Map<String, String> headers, @RequestBody Map<String, Object> body);

    @GetMapping(path = "/api/library/search")
    Object searchGet(@RequestHeader Map<String, String> headers, @RequestParam Map<String, String> queryParams);

    @GetMapping(path = "/api/library/{type}/{id}")
    Object findById(@RequestHeader Map<String, String> headers, @PathVariable("type") String type, @PathVariable("id") String id);

    @GetMapping(path = "/api/library/health")
    Object health(@RequestHeader Map<String, String> headers);
}


