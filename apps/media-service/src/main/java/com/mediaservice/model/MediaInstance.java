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
    @Column(
            name = "instance_id"
    )
    private UUID instanceId;

    @Column(
            name = "file_id",
            nullable = false
    )
    private UUID fileId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    public MediaInstance(UUID fileId, UUID userId) {
        this.fileId = fileId;
        this.userId = userId;
    }

    public MediaInstance() {
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
