package com.libraryservice.dao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DAO for ExerciseDB API.
 * <p>
 * endpoints op basis van de officiële documentatie:
 * <p>
 * Search (fuzzy):
 * GET /api/exercises?search={query}&limit={max}&offset=0
 * Response: { "success": true, "data": [...], "metadata": {...} }
 * <p>
 * Find by ID:
 * GET /api/exercises/{exerciseId}
 * Response: { "success": true, "data": { exerciseId, name, ... } }
 * LET OP: ook hier zit het resultaat in een "data" wrapper.
 */
@Component
public class ExerciseDAO {

    private static final Logger logger = Logger.getLogger(ExerciseDAO.class.getName());

    private final String BASE_URL_EXERCISE = "https://exercisedb.dev";

    private final RestTemplate restTemplate;

    /**
     * Constructor die een nieuwe RestTemplate initialiseert
     * voor het uitvoeren van HTTP requests naar de ExerciseDB API.
     */

    public ExerciseDAO() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Zoekt oefeningen via de ExerciseDB API op basis van een zoekterm.
     * <p>
     * Maakt een GET request naar:
     * /api/v1/exercises?search={query}&limit={limit}&offset=0
     * <p>
     * De API retourneert een response met een "data" veld waarin
     * een lijst van oefeningen zit. Deze methode extraheert en retourneert die lijst.
     *
     * @param query de zoekterm (fuzzy search)
     * @param limit het maximaal aantal resultaten (max 25 volgens API restrictie)
     * @return een lijst van oefeningen als Map-structuren, of een lege lijst bij geen resultaat
     * @throws RuntimeException wanneer de API call faalt
     */
    public List<Map<String, Object>> searchExercises(String query, int limit) {
        String url = UriComponentsBuilder
                .fromUriString(BASE_URL_EXERCISE + "/api/v1/exercises")
                .queryParam("search", query)
                .queryParam("limit", Math.min(limit, 25)) // ExerciseDB max is 25
                .queryParam("offset", 0)
                .toUriString();

        logger.info("ExerciseDAO calling: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("data")) {
                logger.warning("ExerciseDB returned no data for query: " + query);
                return Collections.emptyList();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            logger.info("ExerciseDB returned " + data.size() + " exercises");
            return data;

        } catch (Exception e) {
            logger.severe("ExerciseDB API error: " + e.getMessage());
            throw new RuntimeException("ExerciseDB API error: " + e.getMessage(), e);
        }
    }

    /**
     * Haalt één specifieke oefening op via het unieke exerciseId.
     * <p>
     * Maakt een GET request naar:
     * /api/exercises/{exerciseId}
     * <p>
     * De API-response bevat een wrapper met een "data" object.
     * Deze methode retourneert alleen dat object.
     * <p>
     *
     * @param exerciseId het unieke ID van de oefening
     * @return een Map met de oefening data, of een lege map als niets gevonden is
     * @throws RuntimeException wanneer de API call faalt
     */
    public Map<String, Object> findById(String exerciseId) {
        String url = BASE_URL_EXERCISE + "/api/exercises/" + exerciseId;

        logger.info("ExerciseDAO findById: " + url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("data")) {
                logger.warning("ExerciseDB: geen resultaat voor ID: " + exerciseId);
                return Collections.emptyMap();
            }

            return (Map<String, Object>) response.get("data");

        } catch (Exception e) {
            logger.severe("ExerciseDB findById error: " + e.getMessage());
            throw new RuntimeException("ExerciseDB findById error: " + e.getMessage(), e);
        }
    }
}
