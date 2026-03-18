package com.libraryservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class BookDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookDAO.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    @Autowired
    public BookDAO(
            @Value("${google.books.api.base-url}") String baseUrl,
            @Value("${google.books.api.key}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchBooks(String query, int maxResults) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/volumes")
                .queryParam("q", query)
                .queryParam("maxResults", maxResults)
                .queryParam("key", apiKey)
                .toUriString();

        logger.info("Searching for books with query: {}", query);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("items")) {
                logger.info("No items found");
                return Collections.emptyList();
            }
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            logger.info("Found {} items", items.size());
            return items;
        } catch (Exception e) {
            logger.error("Error while fetching books with query: {}", query, e);
            throw new RuntimeException("Error while fetching books with query: " + query, e);
        }
    }
}