package com.apigateway.apigateway.models;

import com.apigateway.apigateway.enums.ContentFlag;

public class Media {
    public String fileData;
    public String fileThumb;
    public String contentType;
    public ContentFlag contentFlag;

    public Media() {
    }

    public Media(String fileData, String fileThumb, String contentType, ContentFlag contentFlag) {
        this.fileData = fileData;
        this.fileThumb = fileThumb;
        this.contentType = contentType;
        this.contentFlag = contentFlag;
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
