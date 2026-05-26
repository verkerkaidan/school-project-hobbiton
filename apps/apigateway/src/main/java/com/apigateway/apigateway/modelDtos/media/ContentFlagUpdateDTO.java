package com.apigateway.apigateway.modelDtos.media;


import com.apigateway.apigateway.enums.ContentFlag;

import java.util.UUID;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public UUID id;
    public String file;
    public String note;

    public ContentFlagUpdateDTO(UUID id, ContentFlag contentFlag, String note) {
        this.contentFlag = contentFlag;
        this.id = id;
        this.note = note;
    }

    public ContentFlagUpdateDTO(UUID id, String file) {
        this.id = id;
        this.file = file;
    }

    public ContentFlagUpdateDTO() {
    }
}

