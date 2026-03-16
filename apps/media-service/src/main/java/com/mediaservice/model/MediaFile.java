package com.mediaservice.model;

import com.mediaservice.enums.ContentFlag;
import jakarta.persistence.*;

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
            name = "content_type",
            nullable = false
    )
    private String contentType;

    @Column(
            name = "original",
            nullable = false
    )
    private byte[] original;

    @Column(
            name = "thumbnail",
            nullable = true
    )
    private byte[] thumbnail;

    public MediaFile(byte[] original, byte[] hash, ContentFlag contentFlag, String contentType) {
        this.original = original;
        this.hash = hash;
        this.contentFlag = contentFlag;
        this.contentType = contentType;
    }

    public MediaFile() {

    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public ContentFlag getContentFlag() {
        return contentFlag;
    }

    public void setContentFlag(ContentFlag contentFlag) {
        this.contentFlag = contentFlag;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getOriginal() {
        return original;
    }

    public void setOriginal(byte[] original) {
        this.original = original;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }
}
