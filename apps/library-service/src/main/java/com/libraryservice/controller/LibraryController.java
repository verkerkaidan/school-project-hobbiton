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


/**
 * REST Controller voor de Library microservice.
 * <p>
 * Verantwoordelijkheden:
 * - Ontvangen van inkomende HTTP requests
 * - Valideren en doorgeven van data naar de juiste service
 * - Gebruik maken van de SearchServiceFactory om dynamisch de juiste service te selecteren
 * - Consistente API-responses teruggeven via ApiResponse wrapper
 * <p>
 * Ondersteunt meerdere zoektypes (bijv. BOOK, EXERCISE, etc.) via het Strategy + Factory pattern.
 */
@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {
    private static final Logger logger = LoggerFactory.getLogger(LibraryController.class.getName());
    private final SearchServiceFactory factory;

    public LibraryController(BookService bookService, OpenLibraryDAO openLibraryDAO, SearchServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * POST endpoint voor zoeken (body-based request).
     * <p>
     * Flow:
     * 1. Ontvangt een SearchRequest (JSON-body)
     * 2. Haalt de juiste service op via de factory (op basis van type)
     * 3. Voert search uit via de service
     * 4. Wrapt de resultaten in een ApiResponse
     * <p>
     * Voordeel:
     * - Geschikt voor complexere zoekopdrachten (meer parameters mogelijk)
     *
     * @param request bevat type, query en maxResults
     * @return lijst van zoekresultaten + metadata
     */
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

    /**
     * GET endpoint voor zoeken (query parameters).
     * <p>
     * Flow:
     * 1. Ontvangt query parameters (query, type, maxResults)
     * 2. Zet deze om naar een SearchRequest object
     * 3. Gebruikt de factory om de juiste service te selecteren
     * 4. Voert search uit
     * <p>
     * Voordeel:
     * - Handig voor simpele requests en testen via browser/Postman
     *
     * @param query      de zoekterm
     * @param type       type van zoekactie (bijv. BOOK, EXERCISE)
     * @param maxResults maximaal aantal resultaten (default = 10)
     * @return lijst van zoekresultaten
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResult>>> searchGet(
            @RequestParam String query,
            @RequestParam SearchType type,
            @RequestParam(defaultValue = "10") int maxResults) {

        logger.info("GET /search type={} query='{}'", type, query);

        SearchRequest request = new SearchRequest(type, query, maxResults);
        SearchableService service = factory.getService(type);
        List<SearchResult> results = service.search(request);

        return ResponseEntity.ok(
                ApiResponse.success(results, results.size(), "Search completed successfully")
        );
    }


    /**
     * Haalt één specifiek item op via type en ID.
     * <p>
     * Flow:
     * 1. Bepaalt op basis van type welke service gebruikt moet worden
     * 2. Roept findById aan op die service
     * 3. Retourneert resultaat of 404 indien niet gevonden
     *
     * @param type type van resource (bijv. BOOK, EXERCISE)
     * @param id   uniek ID van de resource
     * @return één SearchResult of een foutmelding
     */
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

    /**
     * Health check endpoint.
     * <p>
     * Wordt gebruikt om te controleren of de microservice draait.
     * Handig voor:
     * - Docker health checks
     * - logging tools
     *
     * @return simpele status message
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Library Microservice is running", 1, "OK")
        );
    }
}
