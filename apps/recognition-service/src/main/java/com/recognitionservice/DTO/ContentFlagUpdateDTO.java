package com.recognitionservice.DTO;

import com.recognitionservice.enums.ContentFlag;

import java.util.UUID;

public class ContentFlagUpdateDTO {
    public ContentFlag contentFlag;
    public String hash;
    public String file;
    public String note;

    public ContentFlagUpdateDTO(ContentFlag contentFlag, String hash, String file, String note) {
        this.contentFlag = contentFlag;
        this.hash = hash;
        this.file = file;
        this.note = note;
    }

    public ContentFlagUpdateDTO(String hash, ContentFlag contentFlag, String note) {
        this.note = note;
        this.hash = hash;
        this.contentFlag = contentFlag;
    }

    public ContentFlagUpdateDTO(String file, String hash) {
        this.file = file;
        this.hash = hash;
    }

    public ContentFlagUpdateDTO() {
    }
}
