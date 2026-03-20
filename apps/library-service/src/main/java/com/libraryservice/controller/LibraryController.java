package com.libraryservice.controller;

import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.dto.common.ApiResponse;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;
import com.libraryservice.factory.SearchServiceFactory;
import com.libraryservice.service.BookService;
import com.libraryservice.service.SearchableService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class LibraryController {
    private static final Logger logger = LoggerFactory.getLogger(LibraryController.class.getName());
    private final SearchServiceFactory factory;


    public LibraryController(BookService bookService, OpenLibraryDAO openLibraryDAO, SearchServiceFactory factory) {
        this.factory = factory;
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResult>>> search(
            @Valid @RequestBody SearchRequest request) {
        logger.info("POST /search type={} query='{}'", request.getType(), request.getQuery());

        SearchableService service = factory.getService(request.getType());
        List<SearchResult> results = service.search(request);
        return ResponseEntity.ok(
                ApiResponse.success(results, results.size(), "Search completed successfully")
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResult>>> searchBooksGet(
            @RequestParam String query,
            @RequestParam SearchType type,
            @RequestParam(defaultValue = "all") String source,
            @RequestParam(defaultValue = "10") int maxResults) {

        logger.info("GET /search type={} query='{}'", type, query);

        SearchRequest request = new SearchRequest(type, query, source, maxResults);
        SearchableService service = factory.getService(type);
        List<SearchResult> results = service.search(request);

        return ResponseEntity.ok(
                ApiResponse.success(results, results.size(), "Search completed successfully")
        );
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<SearchResult>> findById(
            @PathVariable SearchType type,
            @PathVariable String id) {

        logger.info("GET /{}/{}", type, id);

        SearchableService service = factory.getService(type);
        SearchResult result = service.findById(id);

        if (result == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No result found for id: " + id));
        }

        return ResponseEntity.ok(ApiResponse.success(result, 1, "Found"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Library Microservice is running", 1, "OK")
        );
    }
}
