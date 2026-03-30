package com.libraryservice.strategies;

import com.libraryservice.dao.ExerciseDAO;
import com.libraryservice.model.Exercise;
import com.libraryservice.strategy.exercise.ExerciseDbStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExerciseDBStrategyTest {

    private ExerciseDAO exerciseDAO;
    private ExerciseDbStrategy strategy;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(ExerciseDbStrategy.class.getName());
        logger.setLevel(Level.WARNING);
        exerciseDAO = mock(ExerciseDAO.class);
        strategy = new ExerciseDbStrategy(exerciseDAO);
    }

    @Test
    void searchExercises_returnsMappedExercises() {
        Map<String, Object> rawExercise = getRawExercise();

        when(exerciseDAO.searchExercises("Push", 5)).thenReturn(List.of(rawExercise));

        List<Exercise> exercises = strategy.searchExercises("Push", 5);

        assertEquals(1, exercises.size());
        Exercise exercise = exercises.getFirst();
        assertEquals("ex1", exercise.getExerciseId());
        assertEquals("Push-up", exercise.getName());
        assertEquals(List.of("Chest"), exercise.getBodyParts());
        assertEquals(List.of("Pectorals"), exercise.getTargetMuscles());
    }

    @Test
    void findById_returnsExercise_ifFound() {
        Map<String, Object> rawExercise = new HashMap<>();
        rawExercise.put("exerciseId", "ex1");
        rawExercise.put("name", "Push-up");
        rawExercise.put("bodyParts", List.of("Chest"));
        rawExercise.put("targetMuscles", List.of("Pectorals"));
        rawExercise.put("secondaryMuscles", List.of("Triceps"));
        rawExercise.put("equipments", List.of("None"));
        rawExercise.put("exerciseType", "Strength");
        rawExercise.put("overview", "Do a push-up");
        rawExercise.put("instructions", List.of("Step1", "Step2"));
        rawExercise.put("exerciseTips", List.of("Keep back straight"));
        rawExercise.put("gifUrl", "pushup.gif");

        when(exerciseDAO.findById("ex1")).thenReturn(rawExercise);

        Exercise exercise = strategy.findById("ex1");

        assertNotNull(exercise);
        assertEquals("ex1", exercise.getExerciseId());
        assertEquals("Push-up", exercise.getName());
        assertEquals(List.of("Chest"), exercise.getBodyParts());
    }

    @Test
    void findById_returnsNull_ifNotFound() {
        when(exerciseDAO.findById("unknown")).thenReturn(null);

        Exercise exercise = strategy.findById("unknown");

        assertNull(exercise);
    }

    private static Map<String, Object> getRawExercise() {
        Map<String, Object> rawExercise = new HashMap<>();
        rawExercise.put("exerciseId", "ex1");
        rawExercise.put("name", "Push-up");
        rawExercise.put("bodyParts", List.of("Chest"));
        rawExercise.put("targetMuscles", List.of("Pectorals"));
        rawExercise.put("secondaryMuscles", List.of("Triceps"));
        rawExercise.put("equipments", List.of("None"));
        rawExercise.put("exerciseType", "Strength");
        rawExercise.put("overview", "Do a push-up");
        rawExercise.put("instructions", List.of("Step1", "Step2"));
        rawExercise.put("exerciseTips", List.of("Keep back straight"));
        rawExercise.put("gifUrl", "pushup.gif");
        return rawExercise;
    }
}