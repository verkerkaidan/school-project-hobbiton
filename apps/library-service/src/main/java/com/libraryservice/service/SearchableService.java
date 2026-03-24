package com.libraryservice.service;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;

import java.util.List;

/**
 * FACTORY PATTERN
 * <p>
 * WAAROM SearchResult (generiek)?
 * Boeken en oefeningen hebben volledig verschillende velden.
 * SearchResult maakt het mogelijk dat elk domein zijn eigen DTO-type gebruikt,
 * terwijl het toch binnen dezelfde response structuur (ApiResponse) past.
 * <p>
 * Deze interface definieert de standaard contracten voor zoekservices:
 * - search() → zoekopdracht op basis van een query
 * - findById() → ophalen van één item op basis van ID
 */
public interface SearchableService {
    SearchType getSearchType();

    List<SearchResult> search(SearchRequest request);

    SearchResult findById(String id);
}
