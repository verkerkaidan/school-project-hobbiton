package com.libraryservice.model;

import java.util.List;

public class Exercise {
    private String exerciseId;
    private String name;
    private List<String> bodyParts;
    private List<String> targetMuscles;
    private List<String> secondaryMuscles;
    private List<String> equipments;
    private String exerciseType;
    private String overview;
    private List<String> instructions;
    private List<String> exerciseTips;
    private String imageUrl;
    private String videoUrl;

    public Exercise() {
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

    public List<String> getExerciseTips() {
        return exerciseTips;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBodyParts(List<String> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public void setTargetMuscles(List<String> targetMuscles) {
        this.targetMuscles = targetMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public void setEquipments(List<String> equipments) {
        this.equipments = equipments;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public void setExerciseTips(List<String> exerciseTips) {
        this.exerciseTips = exerciseTips;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

