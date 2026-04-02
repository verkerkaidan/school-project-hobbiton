package com.apigateway.apigateway.modelDtos.posts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GymDTO.class,     name = "GYM"),
        @JsonSubTypes.Type(value = ReadingDTO.class, name = "READING")
})
public abstract class PostDTO {
    public UUID id;
    public String description;
    public String title;
    public UUID userId;
    public String hobbyType;
    public Set<String> mediaInstanceDTOSet;
    public Set<UUID> mediaInstanceSet;
}
