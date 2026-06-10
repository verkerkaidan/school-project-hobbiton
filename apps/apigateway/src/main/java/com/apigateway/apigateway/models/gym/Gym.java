package com.apigateway.apigateway.models.gym;

import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.MediaInstance;
import com.apigateway.apigateway.models.Post;

import java.time.LocalDateTime;
import java.util.*;

public class Gym extends Post {

    private List<GymExercise> exercises = new ArrayList<>();

    private double totalDuration;// in minutes

    public Gym() {

    }

    public Gym(UUID id, String description, String title, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt, List<MediaInstance> mediaInstanceSet, String hobbyType, List<GymExercise> exercises, double totalDuration, Interaction interaction) {
        super(id, description, title, userId, createdAt, updatedAt, mediaInstanceSet, hobbyType, interaction);
        this.exercises = exercises;
        this.totalDuration = totalDuration;
    }

    public List<GymExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<GymExercise> exercises) {
        this.exercises = exercises;
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
    }
}
