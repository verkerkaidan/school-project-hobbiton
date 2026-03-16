package com.mediaservice.dto;

import com.mediaservice.enums.ContentFlag;

import java.util.UUID;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public UUID fileId;
    public byte[] file;

    public ContentFlagUpdateDTO(ContentFlag contentFlag, UUID fileId) {
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
