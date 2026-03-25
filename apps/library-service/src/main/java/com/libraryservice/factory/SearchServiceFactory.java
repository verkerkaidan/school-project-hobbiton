package com.libraryservice.factory;

import com.libraryservice.enums.SearchType;
import com.libraryservice.service.SearchableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory voor het ophalen van de juiste SearchableService op basis van SearchType.
 * <p>
 * Werking:
 * - Spring injecteert automatisch alle beans die SearchableService implementeren
 * - Deze factory selecteert de juiste service op basis van het type (bijv. BOOK, EXERCISE)
 * <p>
 * Design pattern:
 * - Factory Pattern: bepaalt welke concrete service wordt gebruikt
 * - Strategy Pattern: elke service bevat zijn eigen zoeklogica
 * <p>
 * Voordeel:
 * - Geen if/else of switch statements nodig in de controller
 * - Makkelijk uitbreidbaar (nieuwe service toevoegen = automatisch beschikbaar)
 */
@Component
public class SearchServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceFactory.class.getName());
    private final List<SearchableService> services;

    public SearchServiceFactory(List<SearchableService> services) {
        this.services = services;
    }

    /**
     * Geeft de juiste SearchableService terug op basis van het opgegeven type.
     *
     * @param type het type zoekactie (bijv. BOOK, EXERCISE)
     * @return de bijbehorende SearchableService implementatie
     * @throws IllegalArgumentException als er geen service gevonden wordt voor het type
     */
    public SearchableService getService(SearchType type) {
        logger.debug("Getting service for type {}", type);
        return services.stream()
                .filter(s -> s.getSearchType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No service found for type: " + type));
    }
}
