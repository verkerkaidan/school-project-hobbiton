package com.apigateway.apigateway.DTO;


import com.apigateway.apigateway.enums.ContentFlag;

import java.util.UUID;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public UUID fileId;
    public byte[] file;
    public String note;

    public ContentFlagUpdateDTO(UUID fileId, ContentFlag contentFlag, String note) {
        this.contentFlag = contentFlag;
        this.fileId = fileId;
        this.note = note;
    }

    public ContentFlagUpdateDTO(UUID fileId, byte[] file) {
        this.fileId = fileId;
        this.file = file;
    }

    public ContentFlagUpdateDTO() {
    }
}

