package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.chats.AlterUserDTO;
import com.apigateway.apigateway.modelDtos.chats.ChatDTO;
import com.apigateway.apigateway.modelDtos.chats.TitleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "chat-service", url = "${chat.http-url}")
public interface ChatClient {

    @GetMapping("/allowed-users/{chatId}")
    List<String> getAllowedUsers(
            @PathVariable("chatId") String chatId,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/get-chats/{userId}")
    List<Object> getChats(
            @PathVariable("userId") String userId,
            @RequestHeader("Authorization") String authorization
    );

    @PostMapping("/create-chat")
    String createChat(
            @RequestBody ChatDTO chatDTO,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/get-chat-title/{chatId}")
    String getChatTitle(
            @PathVariable("chatId") String chatId,
            @RequestHeader("Authorization") String authorization
    );

    @PutMapping("/update-title")
    String updateChatTitle(
            @RequestBody TitleDTO titleDTO,
            @RequestHeader("Authorization") String authorization
    );

    @PutMapping("/delete-user-from-chat")
    Boolean deleteUserFromChat(
            @RequestBody AlterUserDTO alterUserDTO,
            @RequestHeader("Authorization") String authorization
    );

    @PutMapping("/add-user-to-chat")
    Boolean addUserToChat(
            @RequestBody AlterUserDTO alterUserDTO,
            @RequestHeader("Authorization") String authorization
    );

    @DeleteMapping("/delete-user?deleteUserId={userId}")
    void deleteUserFromAllChats(
            @PathVariable("userId") String userId
    );
}