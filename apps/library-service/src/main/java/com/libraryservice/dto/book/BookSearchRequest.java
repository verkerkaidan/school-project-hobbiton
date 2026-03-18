package com.libraryservice.dto.book;

import com.libraryservice.enums.BookCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BookSearchRequest {

    @NotBlank(message = "Search query must not be blank")
    private String query;

    private BookCategory category = BookCategory.ALL;

    @Min(value = 1, message = "Max results must be at least 1")
    @Max(value = 40, message = "Max results cannot exceed 40 (Google Books API limit)")
    private int maxResults = 10;

    // -------------------------
    // Constructors
    // -------------------------

    public BookSearchRequest() {}

    public BookSearchRequest(String query, BookCategory category, int maxResults) {
        this.query = query;
        this.category = category;
        this.maxResults = maxResults;
    }

    // -------------------------
    // Getters
    // -------------------------

    public String getQuery() { return query; }
    public BookCategory getCategory() { return category; }
    public int getMaxResults() { return maxResults; }

    // -------------------------
    // Setters
    // -------------------------

    public void setQuery(String query) { this.query = query; }
    public void setCategory(BookCategory category) { this.category = category; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
}
