package com.libraryservice.dto.common;

import com.libraryservice.enums.SearchType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


/**
 * GECENTRALISEERDE SEARCH REQUEST
 * <p>
 * Dit is de enige request body voor:
 * POST /API/v1/library/search
 * <p>
 * Het veld 'type' bepaalt via de Factory welke service gebruikt wordt.
 * Voorbeeld voor boeken:
 * {
 * "type": "BOOK",
 * "query": "clean code",
 * "maxResults": 10
 * }
 * <p>
 * Voorbeeld voor oefeningen:
 * {
 * "type": "EXERCISE",
 * "query": "bench press",
 * "maxResults": 10
 * }
 * <p>
 * WAAROM ÉÉN GEDEELDE REQUEST DTO?
 * - Eén endpoint, één uniforme request structuur
 * - Het 'type' veld bepaalt de routing naar de juiste service
 * - Eenvoudig voor de client: altijd dezelfde endpoint gebruiken
 */

public class SearchRequest {

    @NotNull(message = "Type is required: BOOK, EXERCISE, MOVIE")
    private SearchType type;

    @NotBlank(message = "Query must not be empty")
    private String query;

    @Min(value = 1, message = "Minimaal 1 resultaat")
    @Max(value = 40, message = "Maximaal 40 resultaten")
    private int maxResults = 10;

    public SearchRequest(SearchType type, String query, int maxResults) {
        this.type = type;
        this.query = query;
        this.maxResults = maxResults;
    }

    public SearchRequest() {
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        this.type = type;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getQuery() {
        return query;
    }
}
