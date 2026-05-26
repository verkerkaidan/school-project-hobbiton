package com.apigateway.apigateway.modelDtos.media;

import java.util.List;
import java.util.UUID;

public class MediaUploadResponse {
    public List<MediaResult> results;

    public static class MediaResult {
        public UUID id;
        public String status;
        public String reason;
    }
}
