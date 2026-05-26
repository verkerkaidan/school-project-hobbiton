package com.apigateway.apigateway.modelDtos.media;

import io.netty.handler.codec.http.multipart.FileUpload;

public class MediaEntryDTO {
    public FileUpload fileData;

    public MediaEntryDTO(FileUpload fileData) {
        this.fileData = fileData;
    }

    public MediaEntryDTO() {
    }
}
