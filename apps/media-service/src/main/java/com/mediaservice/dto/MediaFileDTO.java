package com.mediaservice.dto;

import com.mediaservice.enums.ContentFlag;

public class MediaFileDTO {
    public byte[] fileData;
    public byte[] fileThumb;
    public String contentType;
    public ContentFlag contentFlag;

    public MediaFileDTO(byte[] fileData, byte[] fileThumb, String contentType, ContentFlag contentFlag) {
        this.fileData = fileData;
        this.fileThumb = fileThumb;
        this.contentType = contentType;
        this.contentFlag = contentFlag;
    }
}
