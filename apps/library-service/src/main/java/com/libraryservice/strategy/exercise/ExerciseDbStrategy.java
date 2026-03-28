package com.libraryservice.strategy.exercise;

import com.libraryservice.dao.ExerciseDAO;
import com.libraryservice.model.Exercise;
import org.springframework.stereotype.Component;

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
    @Override
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
                (String) raw.get("exerciseId"),
                (String) raw.get("name"),
                (List<String>) raw.get("bodyParts"),
                (List<String>) raw.get("targetMuscles"),
                (List<String>) raw.get("secondaryMuscles"),
                (List<String>) raw.get("equipments"),
                (String) raw.get("exerciseType"),
                (String) raw.get("overview"),
                (List<String>) raw.get("instructions"),
                (List<String>) raw.get("exerciseTips"),
                (String) raw.get("gifUrl")
        );
    }
}
