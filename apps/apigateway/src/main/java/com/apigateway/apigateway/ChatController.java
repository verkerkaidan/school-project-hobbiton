package com.apigateway.apigateway;

import com.apigateway.apigateway.modelDtos.chats.ChatDTO;
import com.apigateway.apigateway.modelDtos.chats.TitleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Controller
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:19293"})
public class ChatController {

    private final WebClient http;

    public ChatController(@Value("${chat.http-url}") String httpUrl) {
        this.http = WebClient.create(httpUrl);
    }

    @GetMapping("/get-chats/{userId}")
    public Mono<ResponseEntity<String>> getChats(@PathVariable String userId) {
        return http.get()
                .uri("/get-chats/{userId}", userId)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/create-chat")
    public Mono<ResponseEntity<String>> createChat(@RequestBody ChatDTO chatDTO) {
         return http.post()
                 .uri("/create-chat")
                 .bodyValue(chatDTO)
                 .retrieve()
                 .toEntity(String.class);
    }
    @GetMapping("/get-chat-title/{chatId}")
    public Mono<ResponseEntity<String>> getChatTitle(@PathVariable String chatId) {
        return http.get()
                .uri("/get-chat-title/{chatId}", chatId)
                .retrieve()
                .toEntity(String.class);
    }

    @PutMapping("/update-title")
    public Mono<ResponseEntity<String>> updateChatTitle(@RequestBody TitleDTO titleDTO) {
        return http.put()
                .uri("/update-title")
                .bodyValue(titleDTO)
                .retrieve()
                .toEntity(String.class);
    }
}