package com.apigateway.apigateway.modelDtos.media;

import com.apigateway.apigateway.enums.ContentFlag;

public class FullMediaFile {
    public String hash;
    public String file;
    public ContentFlag contentFlag;
    public String contentType;

    public FullMediaFile(ContentFlag contentFlag, String contentType, String file, String hash) {
        this.contentFlag = contentFlag;
        this.contentType = contentType;
        this.file = file;
        this.hash = hash;
    }

    public FullMediaFile() {
    }
}
