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

    public Exercise(String exerciseId, String name, List<String> bodyParts, List<String> targetMuscles, List<String> secondaryMuscles, List<String> equipments, String exerciseType, String overview, List<String> instructions, List<String> exerciseTips, String imageUrl, String videoUrl) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.bodyParts = bodyParts;
        this.targetMuscles = targetMuscles;
        this.secondaryMuscles = secondaryMuscles;
        this.equipments = equipments;
        this.exerciseType = exerciseType;
        this.overview = overview;
        this.instructions = instructions;
        this.exerciseTips = exerciseTips;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
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

    public List<String> getExerciseTips() {
        return exerciseTips;
    }

    public void setExerciseTips(List<String> exerciseTips) {
        this.exerciseTips = exerciseTips;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

