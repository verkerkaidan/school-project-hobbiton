package com.apigateway.apigateway.modelDtos.media;

import com.apigateway.apigateway.enums.ContentFlag;

import java.util.UUID;

public class MediaDTO {
    public UUID relevantId;
    public String fileData;
    public String fileThumb;
    public String contentType;
    public ContentFlag contentFlag;

    public MediaDTO() {
    }

    public MediaDTO(UUID relevantId, String fileData, String fileThumb, String contentType, ContentFlag contentFlag) {
        this.relevantId = relevantId;
        this.fileData = fileData;
        this.fileThumb = fileThumb;
        this.contentType = contentType;
        this.contentFlag = contentFlag;
    }

    public MediaDTO(String fileData, String fileThumb, String contentType, ContentFlag contentFlag) {
        this.fileData = fileData;
        this.fileThumb = fileThumb;
        this.contentType = contentType;
        this.contentFlag = contentFlag;
    }

    public UUID getRelevantId() {
        return relevantId;
    }

    public void setRelevantId(UUID relevantId) {
        this.relevantId = relevantId;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public String getFileThumb() {
        return fileThumb;
    }

    public void setFileThumb(String fileThumb) {
        this.fileThumb = fileThumb;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ContentFlag getContentFlag() {
        return contentFlag;
    }

    public void setContentFlag(ContentFlag contentFlag) {
        this.contentFlag = contentFlag;
    }
}
