package com.apigateway.apigateway.modelDtos.posts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkedHobbyItemDTO {
    public UUID id;
    public String displayName;
    public String imageUrl;
    public String externalId;
    public String hobbyType;  // "BOOK" | "MOVIE" | "EXERCISE"
}