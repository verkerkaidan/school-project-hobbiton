package com.postservice.postservice.models.gym;

import com.postservice.postservice.dto.GymDTO;
import com.postservice.postservice.dto.GymExcerciseDTO;
import com.postservice.postservice.models.Post;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Gym extends Post {

    // One gym session owns many exercises
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "post")
    private Set<GymExcercise> exercises = new HashSet<>();

    @Column(name = "total_duration")
    private double totalDuration;// in minutes

    public Gym() {

    }

    public Gym(GymDTO gymDTO) {
        super(gymDTO);
        super.setHobbyType("GYM");
        for(GymExcerciseDTO gymExcerciseDTO : gymDTO.excercises) {
            this.exercises.add(new GymExcercise(gymExcerciseDTO));
        }
        this.totalDuration = gymDTO.totalDuration;
    }

    public Set<GymExcercise> getExercises() {
        return exercises;
    }

    public void setExercises(Set<GymExcercise> exercises) {
        this.exercises = exercises;
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
    }
}
