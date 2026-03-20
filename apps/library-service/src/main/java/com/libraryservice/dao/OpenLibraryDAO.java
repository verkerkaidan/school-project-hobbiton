package com.libraryservice.dao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DAO for Open Library API.
 * <p>
 * Search:  GET <a href="https://openlibrary.org/search.json?q=...&limit=10">...</a>
 * Response: { "docs": [ {...} ] }
 * <p>
 * ISBN:    GET <a href="https://openlibrary.org/api/books?bibkeys=ISBN:xxx&format=json&jscmd=data">...</a>
 * Response: { "ISBN:xxx": { "title": "...", "authors": [...] } }
 * <p>
 * No API key required — Open Library is fully open.
 */
@Component
public class OpenLibraryDAO {

    private static final Logger logger = Logger.getLogger(OpenLibraryDAO.class.getName());
    private static final String BASE_URL = "https://openlibrary.org";

    private final RestTemplate restTemplate;

    public OpenLibraryDAO() {
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, Object>> searchBooks(String query, int maxResults) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL + "/search.json")
                    .queryParam("q", query)
                    .queryParam("limit", maxResults)
                    .build()
                    .toUri();

            logger.info("Calling OpenLibrary URL: " + uri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warning("OpenLibrary returned status {} for query '{}'");
                return Collections.emptyList();
            }

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("docs")) {
                logger.info("No docs found for query '{}'");
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) body.get("docs");

        } catch (RestClientException e) {
            logger.warning("REST error searching books for query '{}'");
        } catch (Exception e) {
            logger.warning("Unexpected error searching books for query '{}'");
        }

        return Collections.emptyList(); // altijd returnen
    }

    public Map<String, Object> findByIsbn(String isbn) {
        String url = UriComponentsBuilder
                .fromUriString(BASE_URL + "/api/books")
                .queryParam("bibkeys", "ISBN:" + isbn)
                .queryParam("format", "json")
                .queryParam("jscmd", "data")
                .toUriString();

        logger.info("OpenLibraryDAO ISBN lookup: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return (response == null) ? Collections.emptyMap() : response;
        } catch (Exception e) {
            logger.severe("Open Library ISBN error: " + e.getMessage());
            throw new RuntimeException("Open Library ISBN error: " + e.getMessage(), e);
        }
    }
}
