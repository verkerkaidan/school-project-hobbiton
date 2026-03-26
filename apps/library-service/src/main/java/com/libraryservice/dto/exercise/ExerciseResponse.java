package com.libraryservice.dto.exercise;

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
    private String imageUrl;

    public ExerciseResponse(String exerciseId, String name, List<String> bodyParts, List<String> targetMuscles, List<String> secondaryMuscles, List<String> equipments, String exerciseType, String overview, List<String> instructions, String imageUrl) {
    }

    public String getExerciseId() {
        return exerciseId;
    }


    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(List<String> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public List<String> getTargetMuscles() {
        return targetMuscles;
    }

    public void setTargetMuscles(List<String> targetMuscles) {
        this.targetMuscles = targetMuscles;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public List<String> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<String> equipments) {
        this.equipments = equipments;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getResultType() {
        return "exercise";
    }

}
