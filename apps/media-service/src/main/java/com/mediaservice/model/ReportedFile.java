package com.mediaservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class ReportedFile {
    @Id
    @GeneratedValue
    private UUID fileId;
    private byte[] hash;

    public ReportedFile(byte[] hash) {
        this.hash = hash;
    }

    public ReportedFile() {
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }
}
