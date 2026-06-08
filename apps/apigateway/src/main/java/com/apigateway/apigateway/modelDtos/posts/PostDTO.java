package com.apigateway.apigateway.modelDtos.posts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GymDTO.class,     name = "GYM"),
        @JsonSubTypes.Type(value = ReadingDTO.class, name = "READING"),
        @JsonSubTypes.Type(value = OtherDTO.class, name = "OTHER")
})
public abstract class PostDTO {
    public UUID id;
    public String description;
    public String title;
    public UUID userId;
    public String hobbyType;
    public Set<UUID> mediaInstanceSet;
    @JsonIgnore
    public List<MultipartFile> mediaInstanceDTOSet;
}
