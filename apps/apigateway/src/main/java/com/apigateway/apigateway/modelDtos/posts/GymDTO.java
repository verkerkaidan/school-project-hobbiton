package com.apigateway.apigateway.modelDtos.posts;

import java.util.Set;

public class GymDTO extends PostDTO{
    public Set<GymExerciseDTO> exercises;
    public double totalDuration;
}
