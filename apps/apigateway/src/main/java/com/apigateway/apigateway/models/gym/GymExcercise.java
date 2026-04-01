package com.apigateway.apigateway.models.gym;

import java.util.UUID;

public class GymExcercise {
    private UUID id;
    private String title;
    private int reps;
    private double weight;
    private double rpe;

    public GymExcercise() {

    }

    public GymExcercise(UUID id, String title, int reps, double weight, double rpe) {
        this.id = id;
        this.title = title;
        this.reps = reps;
        this.weight = weight;
        this.rpe = rpe;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getRpe() {
        return rpe;
    }

    public void setRpe(double rpe) {
        this.rpe = rpe;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
