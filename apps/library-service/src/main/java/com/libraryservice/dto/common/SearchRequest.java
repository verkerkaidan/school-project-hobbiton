package com.libraryservice.dto.common;

import com.libraryservice.enums.SearchType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * THE UNIFIED SEARCH REQUEST
 * <p>
 * This is the single body for POST /api/v1/library/search.
 * The 'type' field tells the Factory which service to use.
 * The 'source' field tells the Strategy which API to use (books only for now).
 * <p>
 * Example for books:
 * {
 * "type": "BOOK",
 * "query": "clean code",
 * "source": "google",
 * "maxResults": 10
 * }
 * <p>
 * Example for exercises:
 * {
 * "type": "EXERCISE",
 * "query": "bench press",
 * "maxResults": 10
 * }
 * <p>
 * WHY ONE SHARED REQUEST DTO?
 * One endpoint, one request shape. The 'type' field routes everything.
 * Simple for the client — they always POST to the same URL.
 * 'source' is optional and ignored for exercise searches.
 */
public class SearchRequest {

    @NotNull(message = "Type is required: BOOK, EXERCISE, MOVIE")
    private SearchType type;

    @NotBlank(message = "Query must not be empty")
    private String query;

    // Optional: which API source to use. Relevant for BOOK, ignored for EXERCISE.
    // Values: "google", "openlibrary", "all" (default)
    private String source = "all";

    @Min(value = 1, message = "Minimaal 1 resultaat")
    @Max(value = 40, message = "Maximaal 40 resultaten")
    private int maxResults = 10;

    public SearchRequest() {
    }

    public SearchRequest(SearchType type, String query, String source, int maxResults) {
        this.type = type;
        this.query = query;
        this.source = source;
        this.maxResults = maxResults;
    }

    public SearchType getType() {
        return type;
    }

    public String getQuery() {
        return query;
    }

    public String getSource() {
        return source;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setType(SearchType type) {
        this.type = type;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
