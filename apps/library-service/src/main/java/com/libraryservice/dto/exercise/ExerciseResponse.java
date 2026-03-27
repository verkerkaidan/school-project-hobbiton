package com.libraryservice.dto.exercise;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.libraryservice.dto.common.SearchResult;

import java.util.List;

/**
 * Oefening-specifieke response DTO.
 * <p>
 * Implementeert SearchResult — dezelfde interface als BookResponse.
 * Hierdoor kan de controller beide types op een uniforme manier verwerken.
 * <p>
 * Velden komen overeen met de ExerciseDB API response:
 * exerciseId, name, bodyParts, targetMuscles, secondaryMuscles,
 * equipments, exerciseType, overview, instructions, imageUrl
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExerciseResponse implements SearchResult {

    private String exerciseId;
    private String name;
    private List<String> bodyParts;
    private List<String> targetMuscles;
    private List<String> secondaryMuscles;
    private List<String> equipments;
    private String exerciseType;
    private String overview;
    private List<String> instructions;
    private String gifUrl;

    public ExerciseResponse(String exerciseId, String name, List<String> bodyParts, List<String> targetMuscles, List<String> secondaryMuscles, List<String> equipments, String exerciseType, String overview, List<String> instructions, String gifUrl) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.bodyParts = bodyParts;
        this.targetMuscles = targetMuscles;
        this.secondaryMuscles = secondaryMuscles;
        this.equipments = equipments;
        this.exerciseType = exerciseType;
        this.overview = overview;
        this.instructions = instructions;
        this.gifUrl = gifUrl;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public String getName() {
        return name;
    }

    public List<String> getBodyParts() {
        return bodyParts;
    }

    public List<String> getTargetMuscles() {
        return targetMuscles;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public List<String> getEquipments() {
        return equipments;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public String getOverview() {
        return overview;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    @Override
    public String getResultType() {
        return "exercise";
    }

}
