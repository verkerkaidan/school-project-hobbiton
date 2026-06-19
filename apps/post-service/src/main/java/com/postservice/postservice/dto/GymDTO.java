package com.postservice.postservice.dto;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.gym.Gym;

import java.util.HashSet;
import java.util.Set;

public class GymDTO extends PostDTO{
    public Set<GymExerciseDTO> exercises = new HashSet<>();
    public double totalDuration;

    @Override
    public Post toEntity() {
        return new Gym(this); // gym-specific construction lives here
    }
}
