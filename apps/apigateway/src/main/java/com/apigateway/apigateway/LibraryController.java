package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.LibraryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/library")
public class LibraryController {

    private final LibraryClient libraryClient;

    public LibraryController(LibraryClient libraryClient) {
        this.libraryClient = libraryClient;
    }

    @PostMapping("/search")
    public ResponseEntity<Object> searchPost(@RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> body) {
        Map<String, String> filtered = headers.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase("content-length")
                        && !e.getKey().equalsIgnoreCase("host")
                        && !e.getKey().equalsIgnoreCase("transfer-encoding"))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Object resp = libraryClient.searchPost(filtered, body);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchGet(@RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> queryParams) {
        Object resp = libraryClient.searchGet(headers, queryParams);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<Object> findById(@RequestHeader Map<String, String> headers, @PathVariable String type,
            @PathVariable String id) {
        Object resp = libraryClient.findById(headers, type, id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/health")
    public ResponseEntity<Object> health(@RequestHeader Map<String, String> headers) {
        Object resp = libraryClient.health(headers);
        return ResponseEntity.ok(resp);
    }
}
