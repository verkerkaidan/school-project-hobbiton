package com.postservice.postservice.models.gym;

import com.postservice.postservice.dto.GymExcerciseDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class GymExcercise {
    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    private int reps;
    private double weight;
    private double rpe;

    public GymExcercise (GymExcerciseDTO gymExcerciseDTO){
        this.reps = gymExcerciseDTO.reps;
        this.weight = gymExcerciseDTO.weight;
        this.rpe = gymExcerciseDTO.rpe;
        this.title = gymExcerciseDTO.title;
    }

    public GymExcercise() {

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
