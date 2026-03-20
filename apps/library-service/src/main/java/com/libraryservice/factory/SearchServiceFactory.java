package com.libraryservice.factory;

import com.libraryservice.enums.SearchType;
import com.libraryservice.service.SearchableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceFactory.class.getName());
    private final List<SearchableService> services;

    public SearchServiceFactory(List<SearchableService> services) {
        this.services = services;
    }

    public SearchableService getService(SearchType type) {
        logger.debug("Getting service for type {}", type);
        return services.stream()
                .filter(s -> s.getSearchType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No service found for type: " + type));
    }
}
