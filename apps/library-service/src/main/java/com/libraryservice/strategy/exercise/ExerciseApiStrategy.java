package com.libraryservice.strategy.exercise;

import com.libraryservice.model.Exercise;

import java.util.List;

/**
 * STRATEGY PATTERN — interface voor oefening (exercise) API bronnen.
 * <p>
 * Elke externe bron voor oefeningen (bijv. een API, database of toekomstige service)
 * implementeert deze interface.
 * <p>
 * De service laag (bijv. ExerciseService) werkt uitsluitend met deze interface
 * en kent de concrete implementaties niet.
 * <p>
 * Hierdoor is het systeem flexibel en eenvoudig uit te breiden met nieuwe bronnen.
 */
public interface ExerciseApiStrategy {
    List<Exercise> searchExercises(String query, int maxResults);

    Exercise findById(String exerciseId);
}
