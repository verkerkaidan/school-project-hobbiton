package com.postservice.postservice.dto;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.other.Other;

public class OtherDTO extends PostDTO {
    @Override
    public Post toEntity() {
        return new Other(this);
    }
}
