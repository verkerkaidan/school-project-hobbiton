package com.mediaservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class FileReport {
    @GeneratedValue
    @Id
    private UUID reportId;
    private UUID fileId;
    private UUID userID;

    public FileReport(UUID userID, UUID fileId) {
        this.fileId = fileId;
        this.userID = userID;
    }

    public FileReport() {
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }
}
