package com.libraryservice.services;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.dto.exercise.ExerciseResponse;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Exercise;
import com.libraryservice.service.ExerciseService;
import com.libraryservice.strategy.exercise.ExerciseApiStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExerciseServiceTest {

    private ExerciseApiStrategy strategy;
    private ExerciseService service;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(ExerciseService.class.getName());
        logger.setLevel(Level.WARNING);
        strategy = mock(ExerciseApiStrategy.class);
        service = new ExerciseService(strategy);
    }

    @Test
    void search_returnsMappedDTOs() {
        Exercise exercise = new Exercise();
        exercise.setExerciseId("ex1");
        exercise.setName("Push Up");

        when(strategy.searchExercises("Test", 5)).thenReturn(List.of(exercise));

        List<SearchResult> results = service.search(new SearchRequest(SearchType.EXERCISE, "Test", 5));

        assertEquals(1, results.size());
        ExerciseResponse response = (ExerciseResponse) results.getFirst();
        assertEquals("ex1", response.getExerciseId());
        assertEquals("Push Up", response.getName());
    }

    @Test
    void findById_returnsMappedDTO() {
        Exercise exercise = new Exercise();
        exercise.setExerciseId("ex1");
        exercise.setName("Push Up");

        when(strategy.findById("ex1")).thenReturn(exercise);

        SearchResult result = service.findById("ex1");

        assertNotNull(result);
        ExerciseResponse response = (ExerciseResponse) result;
        assertEquals("ex1", response.getExerciseId());
        assertEquals("Push Up", response.getName());
    }

    @Test
    void findById_returnsNull_ifNotFound() {
        when(strategy.findById("unknown")).thenReturn(null);
        assertNull(service.findById("unknown"));
    }
}