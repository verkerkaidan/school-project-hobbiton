package com.mediaservice.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "media_instances"
)
public class MediaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(
            name = "hash",
            nullable = false
    )
    private MediaFile mediaFile;

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    public MediaInstance(MediaFile mediaFile, UUID userId) {
        this.mediaFile = mediaFile;
        this.userId = userId;
    }

    public MediaInstance() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
