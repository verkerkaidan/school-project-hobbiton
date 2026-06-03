package com.apigateway.apigateway.modelDtos.chats;

public class AlterUserDTO {
    public String userId;
    public String chatId;

    public AlterUserDTO(String userId, String chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public String getChatId() {
        return chatId;
    }
}
