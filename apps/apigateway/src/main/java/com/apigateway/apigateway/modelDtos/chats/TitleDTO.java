package com.apigateway.apigateway.modelDtos.chats;

public class TitleDTO {

    String title;
    String chatId;

    public TitleDTO(String title, String chatId) {
        this.title = title;
        this.chatId = chatId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
