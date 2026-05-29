package com.apigateway.apigateway.modelDtos.media;


import com.apigateway.apigateway.enums.ContentFlag;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public String hash;
    public String file;
    public String note;

    public ContentFlagUpdateDTO(String hash, ContentFlag contentFlag, String note) {
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
}

