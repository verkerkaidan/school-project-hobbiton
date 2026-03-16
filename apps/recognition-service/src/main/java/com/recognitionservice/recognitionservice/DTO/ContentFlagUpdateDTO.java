package com.recognitionservice.recognitionservice.DTO;

import com.recognitionservice.recognitionservice.enums.ContentFlag;

import java.util.UUID;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public UUID fileId;
    public byte[] file;

    public ContentFlagUpdateDTO(UUID fileId, ContentFlag contentFlag) {
        this.contentFlag = contentFlag;
        this.fileId = fileId;
    }

    public ContentFlagUpdateDTO(UUID fileId, byte[] file) {
        this.fileId = fileId;
        this.file = file;
    }

    public ContentFlagUpdateDTO() {
    }
}
