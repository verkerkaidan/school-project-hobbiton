package com.apigateway.apigateway.models;

import com.apigateway.apigateway.modelDtos.media.MediaDTO;

public class AdminProfileSummaryResponse {
    public String username;
    public MediaDTO profilePicture;
    public String profilePictureUrl;

    public AdminProfileSummaryResponse() {
    }
}