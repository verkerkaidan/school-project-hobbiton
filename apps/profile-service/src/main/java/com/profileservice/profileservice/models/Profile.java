package com.profileservice.profileservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
public class Profile {
    @Id
    private UUID userId;

    @Column(unique = true)
    private String username;

    private String name;
    private String biography;
    private String mediaPointer;
    private String link;
    private String location;
    private OffsetDateTime lastEdited;

    @Column(name = "created_at")
    public OffsetDateTime created_at = OffsetDateTime.now();

    public Profile(){
    }

    public Profile(UUID userId, String username, String name, String biography, String mediaPointer, String link, String location) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.biography = biography;
        this.mediaPointer = mediaPointer;
        this.link = link;
        this.location = location;
        this.lastEdited = OffsetDateTime.now();
    }
}
