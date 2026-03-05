package com.mediaservice.model;

import com.mediaservice.enums.ContentFlag;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "media_files"
)
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "file_id"
    )
    private UUID fileId;

    @Column(
            name = "hash",
            nullable = false,
            unique = true
    )
    private byte[] hash;

    @Column(
            name = "content_flag",
            nullable = false
    )
    private ContentFlag contentFlag;

    @Column(
            name = "file_data",
            nullable = false
    )
    private String fileData;

    public MediaFile(byte[] hash, ContentFlag contentFlag, String fileData) {
        this.hash = hash;
        this.contentFlag = contentFlag;
        this.fileData = fileData;
    }

    public MediaFile() {

    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public UUID getId() {
        return fileId;
    }

    public void setId(UUID id) {
        this.fileId = id;
    }

    public ContentFlag getContentFlag() {
        return contentFlag;
    }

    public void setContentFlag(ContentFlag contentFlag) {
        this.contentFlag = contentFlag;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }
}
