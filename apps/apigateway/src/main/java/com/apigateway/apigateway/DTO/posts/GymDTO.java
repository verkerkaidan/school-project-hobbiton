package com.apigateway.apigateway.DTO.posts;

import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.gym.Gym;

import java.util.Set;

public class GymDTO extends PostDTO{
    public Set<GymExcerciseDTO> excercises;
    public double totalDuration;
}
