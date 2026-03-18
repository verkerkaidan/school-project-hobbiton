package com.libraryservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OpenLibraryDAO {
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryDAO.class);
    private final RestTemplate restTemplate;

    public OpenLibraryDAO() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchBooks(String query, int maxResults) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String baseUrl = "https://openlibrary.org/search.json";
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("q", encodedQuery)
                    .queryParam("limit", maxResults)
                    .build()
                    .toUri();

            logger.info("Calling OpenLibrary URL: {}", uri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.warn("OpenLibrary returned status {} for query '{}'", response.getStatusCode(), query);
                return Collections.emptyList();
            }

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("docs")) {
                logger.info("No docs found for query '{}'", query);
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) body.get("docs");

        } catch (RestClientException e) {
            logger.error("REST error searching books for query '{}'", query, e);
        } catch (Exception e) {
            logger.error("Unexpected error searching books for query '{}'", query, e);
        }

        return Collections.emptyList();
    }
}