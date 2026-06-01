package com.apigateway.apigateway.modelDtos.chats;

import java.util.List;

public class ChatDTO {
    List<String> userIds;
    String title;

    public ChatDTO(List<String> userIds, String title) {
        this.userIds = userIds;
        this.title = title;
    }

    public List<String> getUserIds() { return userIds; }
    public String getTitle() { return title; }
}