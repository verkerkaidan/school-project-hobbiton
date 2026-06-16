package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.LibraryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/library")
public class ListController {

    private final LibraryClient libraryClient;

    public ListController(LibraryClient libraryClient) {
        this.libraryClient = libraryClient;
    }

    @PostMapping("/users/{userId}/lists")
    public ResponseEntity<Object> createList(@RequestHeader Map<String, String> headers,
                                             @PathVariable UUID userId,
                                             @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(libraryClient.createList(headers, userId, body));
    }

    @GetMapping("/users/{userId}/lists")
    public ResponseEntity<Object> getLists(@RequestHeader Map<String, String> headers,
                                           @PathVariable UUID userId) {
        return ResponseEntity.ok(libraryClient.getLists(headers, userId));
    }

    @DeleteMapping("/users/{userId}/lists/{listId}")
    public ResponseEntity<Object> deleteList(@RequestHeader Map<String, String> headers,
                                             @PathVariable UUID userId,
                                             @PathVariable String listId) {
        return ResponseEntity.ok(libraryClient.deleteList(headers, userId, listId));
    }

    @PostMapping("/lists/{listId}/items")
    public ResponseEntity<Object> addItem(@RequestHeader Map<String, String> headers,
                                          @PathVariable String listId,
                                          @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(libraryClient.addItem(headers, listId, body));
    }

    @GetMapping("/lists/{listId}/items")
    public ResponseEntity<Object> getItems(@RequestHeader Map<String, String> headers,
                                           @PathVariable String listId) {
        return ResponseEntity.ok(libraryClient.getItems(headers, listId));
    }

    @DeleteMapping("/lists/{listId}/items/{itemId}")
    public ResponseEntity<Object> removeItem(@RequestHeader Map<String, String> headers,
                                             @PathVariable String listId,
                                             @PathVariable String itemId) {
        return ResponseEntity.ok(libraryClient.removeItem(headers, listId, itemId));
    }

    @GetMapping("/lists/{listId}/items/resolved")
    public ResponseEntity<Object> getResolvedItems(@RequestHeader Map<String, String> headers,
                                                   @PathVariable String listId) {
        return ResponseEntity.ok(libraryClient.getResolvedItems(headers, listId));
    }
}