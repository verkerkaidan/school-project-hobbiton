package com.postservice.postservice.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.postservice.postservice.models.Post;

import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GymDTO.class,     name = "GYM"),
        @JsonSubTypes.Type(value = ReadingDTO.class, name = "READING"),
        @JsonSubTypes.Type(value = OtherDTO.class, name = "OTHER")  // ← new
})
public abstract class PostDTO {
    public UUID id;
    public String description;
    public String title;
    public UUID userId;
    public String hobbyType;
    public Set<UUID> mediaInstanceSet;
    public abstract Post toEntity();
    public Set<LinkedHobbyItemDTO> linkedItems;
}