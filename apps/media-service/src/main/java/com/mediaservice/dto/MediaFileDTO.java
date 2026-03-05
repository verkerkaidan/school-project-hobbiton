package com.mediaservice.dto;

import com.mediaservice.enums.ContentFlag;

public class MediaFileDTO {
    public String fileData;
    public ContentFlag contentFlag;

    public MediaFileDTO(String fileData, ContentFlag contentFlag) {
        this.fileData = fileData;
        this.contentFlag = contentFlag;
    }
}
