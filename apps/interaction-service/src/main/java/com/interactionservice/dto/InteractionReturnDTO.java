package com.interactionservice.dto;

import com.interactionservice.model.Comment;

import java.util.List;

public class InteractionReturnDTO {
    public int likes;
    public List<Comment> comments;

    public InteractionReturnDTO(int likes, List<Comment> comments) {
        this.likes = likes;
        this.comments = comments;
    }
}
