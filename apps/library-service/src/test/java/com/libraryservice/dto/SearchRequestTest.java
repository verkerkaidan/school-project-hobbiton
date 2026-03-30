package com.libraryservice.dto;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.enums.SearchType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SearchRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        Logger logger = Logger.getLogger(SearchRequestTest.class.getName());
        logger.setLevel(java.util.logging.Level.WARNING);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validSearchRequest_noViolations() {
        SearchRequest request = new SearchRequest(SearchType.BOOK, "Clean Code", 10);
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullType_violatesNotNull() {
        SearchRequest request = new SearchRequest(null, "Query", 10);
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Type is required: BOOK, EXERCISE, MOVIE", violations.iterator().next().getMessage());
    }

    @Test
    void blankQuery_violatesNotBlank() {
        SearchRequest request = new SearchRequest(SearchType.BOOK, "   ", 10);
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Query must not be empty", violations.iterator().next().getMessage());
    }

    @Test
    void maxResults_belowMin_violatesMin() {
        SearchRequest request = new SearchRequest(SearchType.BOOK, "Query", 0);
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Minimaal 1 resultaat", violations.iterator().next().getMessage());
    }

    @Test
    void maxResults_aboveMax_violatesMax() {
        SearchRequest request = new SearchRequest(SearchType.BOOK, "Query", 50);
        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Maximaal 40 resultaten", violations.iterator().next().getMessage());
    }

    @Test
    void boundaryValues_pass() {
        // Ondergrens
        SearchRequest minRequest = new SearchRequest(SearchType.EXERCISE, "Push Up", 1);
        assertTrue(validator.validate(minRequest).isEmpty());

        // Bovengrens
        SearchRequest maxRequest = new SearchRequest(SearchType.EXERCISE, "Pull Up", 40);
        assertTrue(validator.validate(maxRequest).isEmpty());
    }
}