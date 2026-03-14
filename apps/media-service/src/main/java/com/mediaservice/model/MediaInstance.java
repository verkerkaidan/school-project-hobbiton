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
            name = "owner_id",
            nullable = false
    )
    private UUID ownerId;

    public MediaInstance(UUID fileId, UUID ownerId) {
        this.fileId = fileId;
        this.ownerId = ownerId;
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}
