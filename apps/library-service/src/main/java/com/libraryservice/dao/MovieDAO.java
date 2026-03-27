package com.libraryservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DAO voor de OMDb (Open Movie Database) API.
 * <p>
 * Endpoints op basis van de officiële OMDb documentatie:
 * <p>
 * Search (by title):
 * GET /?apikey={key}&s={query}&page={page}
 * <p>
 * Find by ID:
 * GET /?apikey={key}&i={movieId}&plot=full
 * <p>
 * LET OP: OMDb vereist dat spaties in de zoekquery worden vervangen door een '+'
 * in plaats van de standaard '%20' encoding. Deze DAO handelt dit specifiek af
 * in de URL-opbouw.
 */
@Component
public class MovieDAO {
    private static final Logger logger = LoggerFactory.getLogger(MovieDAO.class.getName());
    private static final int PAGE_SIZE = 10;

    @Value("${omdb.api.url}")
    private String baseUrl;

    @Value("${omdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    /**
     * Constructor die een nieuwe RestTemplate initialiseert.
     */
    public MovieDAO() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Zoekt films via de OMDb API op basis van een zoekterm.
     * <p>
     * Omdat OMDb resultaten per 10 pagineert, berekent deze methode hoeveel
     * pagina's er nodig zijn om aan de 'maxResults' te voldoen.
     *
     * @param query      De zoekterm voor de filmtitel.
     * @param maxResults Het gewenste aantal resultaten.
     * @return Een gecombineerde lijst van film-maps, gelimiteerd tot maxResults.
     */
    public List<Map<String, Object>> searchMovies(String query, int maxResults) {
        List<Map<String, Object>> results = new ArrayList<>();

        int totalPages = (int) Math.ceil((double) maxResults / PAGE_SIZE);
        for (int page = 1; page <= totalPages; page++) {
            List<Map<String, Object>> pageResults = fetchPage(query, page);

            if (pageResults.isEmpty()) {
                break;
            }
            results.addAll(pageResults);

            if (results.size() >= maxResults) {
                break;
            }
        }

        return results.subList(0, Math.min(results.size(), maxResults));
    }

    /**
     * Haalt één specifieke pagina met zoekresultaten op van OMDb.
     * <p>
     * Vervangt spaties in de query door '+' om te voldoen aan OMDb specificaties.
     *
     * @param query De zoekterm.
     * @param page  Het paginanummer (startend bij 1).
     * @return Een lijst van films op deze pagina, of een lege lijst bij geen resultaat.
     * @throws RuntimeException Wanneer de API-aanroep technisch faalt.
     */
    public List<Map<String, Object>> fetchPage(String query, int page) {
        String formattedQuery = query.trim().replace(" ", "+");

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("apikey", apiKey)
                .queryParam("s", formattedQuery)
                .queryParam("page", page)
                .build(true)
                .toUriString();

        logger.info("OMDb Search calling: {}", url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"True".equals(response.get("Response"))) {
                logger.warn("OMDb Search: no results for '{}' on page {}", query, page);
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) response.getOrDefault("Search", Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error in OMDb Search for '{}': {}", query, e.getMessage());
            throw new RuntimeException("Movie search failed: " + e.getMessage());
        }
    }

    /**
     * Haalt uitgebreide details op van één film via het unieke IMDb ID.
     *
     * @param movieId Het unieke IMDb ID (bijv. tt1234567).
     * @return Een Map met alle filmdetails, of een lege map als de film niet gevonden is.
     * @throws RuntimeException Wanneer de API-aanroep technisch faalt.
     */
    public Map<String, Object> findById(String movieId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("apikey", apiKey)
                .queryParam("i", movieId)
                .queryParam("plot", "full")
                .toUriString();

        logger.info("OMDb findById calling: {}", url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"True".equals(response.get("Response"))) {
                logger.warn("OMDb: No movie found with ID: {}", movieId);
                return Collections.emptyMap();
            }

            return response;
        } catch (Exception e) {
            logger.error("Error in OMDb findById for {}: {}", movieId, e.getMessage());
            throw new RuntimeException("Movie lookup failed: " + e.getMessage());
        }
    }
}