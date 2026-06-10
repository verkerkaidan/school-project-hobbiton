package com.postservice.postservice.models.gym;

import com.postservice.postservice.dto.GymDTO;
import com.postservice.postservice.dto.GymExerciseDTO;
import com.postservice.postservice.models.Post;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Gym extends Post {

    // One gym session owns many exercises
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "post")
    private Set<GymExercise> exercises = new HashSet<>();

    @Column(name = "total_duration")
    private double totalDuration;// in minutes

    public Gym() {

    }

    public Gym(GymDTO gymDTO) {
        super(gymDTO);
        super.setHobbyType("GYM");

        if (gymDTO.exercises != null) {
            for (GymExerciseDTO dto : gymDTO.exercises) {
                this.exercises.add(new GymExercise(dto));
            }
        }

        this.totalDuration = gymDTO.totalDuration;
    }

    public Set<GymExercise> getExercises() {
        return exercises;
    }

    public void setExercises(Set<GymExercise> exercises) {
        this.exercises = exercises;
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
    }
}
