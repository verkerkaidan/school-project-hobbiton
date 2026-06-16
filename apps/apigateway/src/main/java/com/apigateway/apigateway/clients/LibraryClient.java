package com.apigateway.apigateway.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

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

    @PostMapping(path = "/api/library/users/{userId}/lists", consumes = "application/json")
    Object createList(@RequestHeader Map<String, String> headers,
                      @PathVariable("userId") UUID userId,
                      @RequestBody Map<String, Object> body);

    @GetMapping(path = "/api/library/users/{userId}/lists")
    Object getLists(@RequestHeader Map<String, String> headers,
                    @PathVariable("userId") UUID userId);

    @DeleteMapping(path = "/api/library/users/{userId}/lists/{listId}")
    Object deleteList(@RequestHeader Map<String, String> headers,
                      @PathVariable("userId") UUID userId,
                      @PathVariable("listId") String listId);

    @PostMapping(path = "/api/library/lists/{listId}/items", consumes = "application/json")
    Object addItem(@RequestHeader Map<String, String> headers,
                   @PathVariable("listId") String listId,
                   @RequestBody Map<String, Object> body);

    @GetMapping(path = "/api/library/lists/{listId}/items")
    Object getItems(@RequestHeader Map<String, String> headers,
                    @PathVariable("listId") String listId);

    @DeleteMapping(path = "/api/library/lists/{listId}/items/{itemId}")
    Object removeItem(@RequestHeader Map<String, String> headers,
                      @PathVariable("listId") String listId,
                      @PathVariable("itemId") String itemId);

    @GetMapping(path = "/api/library/lists/{listId}/items/resolved")
    Object getResolvedItems(@RequestHeader Map<String, String> headers,
                            @PathVariable("listId") String listId);
}



