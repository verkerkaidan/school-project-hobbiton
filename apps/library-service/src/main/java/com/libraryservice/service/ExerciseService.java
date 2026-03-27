package com.libraryservice.service;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.dto.exercise.ExerciseResponse;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Exercise;
import com.libraryservice.strategy.exercise.ExerciseApiStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service voor het verwerken van exercise zoekopdrachten.
 * <p>
 * Implementeert SearchableService zodat deze via de Factory
 * op dezelfde manier behandeld kan worden als andere services (bijv. BookService).
 * <p>
 * Design pattern:
 * - Strategy Pattern: gebruikt ExerciseApiStrategy voor externe API calls
 * <p>
 * Verantwoordelijkheden:
 * - Aanroepen van de juiste strategy
 * - Omzetten van domain model (Exercise) naar DTO (ExerciseResponse)
 */
@Service
public class ExerciseService implements SearchableService {

    private static final Logger logger = Logger.getLogger(ExerciseService.class.getName());

    private final ExerciseApiStrategy exerciseApiStrategy;

    public ExerciseService(ExerciseApiStrategy exerciseApiStrategy) {
        this.exerciseApiStrategy = exerciseApiStrategy;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.EXERCISE;
    }

    /**
     * Voert een zoekopdracht uit voor oefeningen.
     * <p>
     * Flow:
     * 1. Selecteert een strategy (momenteel de eerste in de lijst)
     * 2. Roept de externe API aan via de strategy
     * 3. Zet de resultaten om naar ExerciseResponse DTO’s
     *
     * @param request bevat zoekterm en maxResults
     * @return lijst van SearchResult (ExerciseResponse)
     */
    @Override
    public List<SearchResult> search(SearchRequest request) {
        logger.info("ExerciseService.search: query='" + request.getQuery() + "'");

        List<Exercise> exercises = exerciseApiStrategy.searchExercises(request.getQuery(), request.getMaxResults());

        return exercises.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Haalt één specifieke oefening op via ID.
     * <p>
     *
     * @param exerciseId uniek ID van de oefening
     * @return SearchResult (ExerciseResponse) of null indien niet gevonden
     */
    @Override
    public SearchResult findById(String exerciseId) {
        logger.info("ExerciseService.findById: " + exerciseId);

        Exercise exercise = exerciseApiStrategy.findById(exerciseId);
        return (exercise != null) ? toResponse(exercise) : null;
    }

    /**
     * Zet een Exercise domain object om naar een ExerciseResponse DTO.
     *
     * @param exercise het domain model
     * @return DTO geschikt voor API-response
     */
    private ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getExerciseId(),
                exercise.getName(),
                exercise.getBodyParts(),
                exercise.getTargetMuscles(),
                exercise.getSecondaryMuscles(),
                exercise.getEquipments(),
                exercise.getExerciseType(),
                exercise.getOverview(),
                exercise.getInstructions(),
                exercise.getGifUrl()
        );
    }
}
