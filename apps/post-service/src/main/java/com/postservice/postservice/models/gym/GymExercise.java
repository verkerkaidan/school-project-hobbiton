package com.postservice.postservice.models.gym;

import com.postservice.postservice.dto.GymExerciseDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class GymExercise {
    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    private int reps;
    private double weight;
    private double rpe;

    public GymExercise(GymExerciseDTO gymExerciseDTO){
        this.reps = gymExerciseDTO.reps;
        this.weight = gymExerciseDTO.weight;
        this.rpe = gymExerciseDTO.rpe;
        this.title = gymExerciseDTO.title;
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
