package com.apigateway.apigateway.models;

import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.models.gym.Gym;
import com.apigateway.apigateway.models.reading.Reading;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "hobbyType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Gym.class, name = "GYM"),
        @JsonSubTypes.Type(value = Reading.class, name = "READING")
})
public abstract class Post {

    private UUID id;

    private String description;

    private String title;

    private UUID userId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<MediaInstance> mediaInstanceSet;
    private String hobbyType;

    private List<MediaDTO> mediaDTOSet;

    private Interaction interaction;

    public Post() {
    }

    public Post(UUID id, String description, String title, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt, List<MediaInstance> mediaInstanceSet, String hobbyType, Interaction interaction) {
        this.id = id;
        this.description = description;
        this.title = title;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.mediaInstanceSet = mediaInstanceSet;
        this.hobbyType = hobbyType;
        this.interaction = interaction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MediaInstance> getMediaInstanceSet() {
        return mediaInstanceSet;
    }

    public String getHobbyType() {
        return hobbyType;
    }

    public void setHobbyType(String hobbyType) {
        this.hobbyType = hobbyType;
    }

    public List<MediaDTO> getMediaSet() {
        return mediaDTOSet;
    }

    public void setMediaSet(List<MediaDTO> mediaDTOSet) {
        this.mediaDTOSet = mediaDTOSet;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public void setInteraction(Interaction interaction) {
        this.interaction = interaction;
    }
}
