package com.libraryservice.service;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService implements SearchableService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class.getName());

    @Override
    public SearchType getSearchType() {
        return null;
    }

    @Override
    public List<SearchResult> search(SearchRequest request) {
        return List.of();
    }

    @Override
    public SearchResult findById(String id) {
        return null;
    }
}
