package com.apigateway.apigateway.models;

import com.apigateway.apigateway.enums.ContentFlag;

import java.util.UUID;

public class Media {
    public UUID relevantId;
    public String fileData;
    public String contentType;
    public ContentFlag contentFlag;

    public Media() {
    }

    public Media(UUID relevantId, String fileData, String contentType, ContentFlag contentFlag) {
        this.relevantId = relevantId;
        this.fileData = fileData;
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
