package com.recognitionservice.DTO;

import com.recognitionservice.enums.ContentFlag;

import java.util.UUID;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public UUID id;
    public byte[] file;
    public String note;

    public ContentFlagUpdateDTO(UUID id, ContentFlag contentFlag, String note) {
        this.contentFlag = contentFlag;
        this.id = id;
        this.note = note;
    }

    public ContentFlagUpdateDTO(UUID id, byte[] file) {
        this.id = id;
        this.file = file;
    }

    public ContentFlagUpdateDTO() {
    }
}
