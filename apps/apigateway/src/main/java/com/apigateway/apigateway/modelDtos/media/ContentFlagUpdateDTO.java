package com.apigateway.apigateway.modelDtos.media;


import com.apigateway.apigateway.enums.ContentFlag;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public String hash;
    public String file;
    public String note;

    public ContentFlagUpdateDTO(ContentFlag contentFlag, String hash, String note) {
        this.contentFlag = contentFlag;
        this.hash = hash;
        this.note = note;
    }

    public ContentFlagUpdateDTO() {
    }

    public ContentFlagUpdateDTO(String file, String hash) {
        this.file = file;
        this.hash = hash;
    }

    public ContentFlagUpdateDTO(ContentFlag contentFlag) {
        this.contentFlag = contentFlag;
    }
}

