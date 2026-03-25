package com.libraryservice.strategy.exercise;

import com.libraryservice.dao.ExerciseDAO;
import com.libraryservice.model.Exercise;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * STRATEGY + ADAPTER.
 * <p>
 * Verantwoordelijkheid:
 * - Ophalen van oefeningen via de ExerciseDAO
 * - Omzetten van ruwe database data naar het interne Exercise model
 * <p>
 * Adapter functie:
 * De data uit de database (Map<String, Object>) wordt hier omgezet
 * naar een gestandaardiseerd Exercise object.
 * <p>
 * Hierdoor blijft de rest van de applicatie (services en controllers)
 * volledig onafhankelijk van de onderliggende data-opslag.
 */
@Component
public class ExerciseDbStrategy implements ExerciseApiStrategy {
    private static final Logger logger = Logger.getLogger(ExerciseDbStrategy.class.getName());

    private final ExerciseDAO exerciseDAO;

    public ExerciseDbStrategy(ExerciseDAO exerciseDAO) {
        this.exerciseDAO = exerciseDAO;
    }

    /**
     * Zoekt oefeningen op basis van een zoekterm.
     *
     * @param query      zoekterm
     * @param maxResults maximaal aantal resultaten
     * @return lijst van Exercise objecten
     */
    public List<Exercise> searchExercises(String query, int maxResults) {
        logger.info("searchExercises query: " + query);
        return exerciseDAO.searchExercises(query, maxResults).stream()
                .map(this::mapToExercise)
                .collect(Collectors.toList());
    }

    /**
     * Haalt één oefening op via een unieke identifier.
     *
     * @param exerciseId unieke ID van de oefening
     * @return Exercise object of null indien niet gevonden
     */
    public Exercise findById(String exerciseId) {
        logger.info("ExerciseDbStrategy: findById " + exerciseId);
        Map<String, Object> raw = exerciseDAO.findById(exerciseId);
        if (raw == null || raw.isEmpty()) return null;
        return mapToExercise(raw);
    }

    /**
     * Zet ruwe data om naar een Exercise object.
     *
     * @param raw ruwe data uit de database
     * @return gestructureerd Exercise object
     */
    private Exercise mapToExercise(Map<String, Object> raw) {
        return new Exercise(
                (String) raw.getOrDefault("exerciseId", "unknown"),
                (String) raw.getOrDefault("name", "Unknown Exercise"),
                (List<String>) raw.getOrDefault("bodyParts", Collections.emptyList()),
                (List<String>) raw.getOrDefault("targetMuscles", Collections.emptyList()),
                (List<String>) raw.getOrDefault("secondaryMuscles", Collections.emptyList()),
                (List<String>) raw.getOrDefault("equipments", Collections.emptyList()),
                (String) raw.getOrDefault("exerciseType", "UNKNOWN"),
                (String) raw.getOrDefault("overview", "No overview available."),
                (List<String>) raw.getOrDefault("instructions", Collections.emptyList()),
                (List<String>) raw.getOrDefault("exerciseTips", Collections.emptyList()),
                (String) raw.getOrDefault("imageUrl", ""),
                (String) raw.getOrDefault("videoUrl", "")
        );
    }
}
