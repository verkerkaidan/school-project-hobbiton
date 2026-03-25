package com.libraryservice.dto.movie;

import com.libraryservice.dto.common.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieResponse implements SearchResult {
    private static final Logger logger = LoggerFactory.getLogger(MovieResponse.class.getName());

    @Override
    public String getResultType() {
        return "movie";
    }
}
