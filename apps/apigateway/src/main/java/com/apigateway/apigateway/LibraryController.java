package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.LibraryClient;
import com.apigateway.apigateway.models.User;
import com.apigateway.apigateway.service.ProfileAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/library")
public class LibraryController {

    private static final Logger logger = LoggerFactory.getLogger(LibraryController.class);

    private final LibraryClient libraryClient;
    private final ProfileAggregationService profileAggregationService;

    public LibraryController(LibraryClient libraryClient, ProfileAggregationService profileAggregationService) {
        this.libraryClient = libraryClient;
        this.profileAggregationService = profileAggregationService;
    }

    @PostMapping("/search")
    public ResponseEntity<Object> searchPost(@RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> body
    ) {
        Map<String, String> filtered = headers.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase("content-length")
                        && !e.getKey().equalsIgnoreCase("host")
                        && !e.getKey().equalsIgnoreCase("transfer-encoding"))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Object resp;
        if (Objects.equals(body.get("type"), "USER")) {
            try {
                User basicResp = profileAggregationService.getProfileByUsername(body.get("query").toString());
                if (basicResp != null) {
                    resp = Map.of(
                            "success", true,
                            "data", basicResp
                    );
                }
                else {
                    resp = Map.of(
                            "success", false,
                            "message", "Profile couldn't be found"
                    );
                }
            } catch (Exception e) {
                resp = new Object();
            }
        } else {
            try {
                resp = libraryClient.searchPost(filtered, body);
            } catch (Exception e) {
                logger.error("[LIBRARYCONTROLLER searchPost] Library service could not be reached --- \n{}", e.getMessage());
                resp = new Object();
            }
        }

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchGet(@RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> queryParams
    ) {
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
