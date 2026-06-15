package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.ChatClient;
import com.apigateway.apigateway.modelDtos.chats.AlterUserDTO;
import com.apigateway.apigateway.modelDtos.chats.ChatDTO;
import com.apigateway.apigateway.modelDtos.chats.TitleDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private String bearerToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    @GetMapping("/allowed-users/{chatId}")
    public ResponseEntity<List<String>> getAllowedUsers(
            @PathVariable("chatId") String chatId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.getAllowedUsers(chatId, bearerToken(request)));
    }

    @GetMapping("/get-chats/{userId}")
    public ResponseEntity<List<Object>> getChats(
            @PathVariable String userId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.getChats(userId, bearerToken(request)));
    }

    @PostMapping("/create-chat")
    public ResponseEntity<String> createChat(
            @RequestBody ChatDTO chatDTO,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.createChat(chatDTO, bearerToken(request)));
    }

    @GetMapping("/get-chat-title/{chatId}")
    public ResponseEntity<String> getChatTitle(
            @PathVariable String chatId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.getChatTitle(chatId, bearerToken(request)));
    }

    @PutMapping("/update-title")
    public ResponseEntity<String> updateChatTitle(
            @RequestBody TitleDTO titleDTO,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.updateChatTitle(titleDTO, bearerToken(request)));
    }

    @DeleteMapping("/delete-user-from-chat")
    public ResponseEntity<Boolean> deleteUserFromChat(
            @RequestBody AlterUserDTO alterUserDTO,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.deleteUserFromChat(alterUserDTO, bearerToken(request)));
    }

    @PostMapping("/add-user-to-chat")
    public ResponseEntity<Boolean> addUserToChat(
            @RequestBody AlterUserDTO alterUserDTO,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(chatClient.addUserToChat(alterUserDTO, bearerToken(request)));
    }
}