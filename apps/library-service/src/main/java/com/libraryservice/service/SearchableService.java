package com.libraryservice.service;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;

import java.util.List;

/**
 * FACTORY PATTERN
 * <p>
 * WHY SearchResult<T> GENERIC?
 * Books and exercises have totally different fields.
 * SearchResult<T> lets each domain return its own DTO type
 * while still fitting the shared response envelope (ApiResponse).
 * <p>
 * The two methods cover your requirements:
 * search()      — text search (POST /search with a query)
 * findById()    — lookup by ID (GET /library/{type}/{id})
 */
public interface SearchableService {

    /**
     * Which SearchType this service handles.
     * The factory uses this to match the right service.
     */
    SearchType getSearchType();

    /**
     * Searches for items based on the request query.
     */
    List<SearchResult> search(SearchRequest request);

    /**
     * Finds a single item by its identifier.
     * For books: ISBN. For exercises: exerciseId.
     */
    SearchResult findById(String id);
}
