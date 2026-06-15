package com.apigateway.apigateway;

import com.apigateway.apigateway.modelDtos.chats.AlterUserDTO;
import com.apigateway.apigateway.modelDtos.chats.ChatDTO;
import com.apigateway.apigateway.modelDtos.chats.TitleDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@Controller
public class ChatController {

    private final WebClient http;

    public ChatController(@Value("${chat.http-url}") String httpUrl) {
        this.http = WebClient.create(httpUrl);
    }

    private String bearerToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("Forwarding Authorization: " + token);
        return token;
    }

    @GetMapping("/allowed-users/{chatId}")
    public Mono<ResponseEntity<List<String>>> getAllowedUsers(
            @PathVariable("chatId") String chatId,
            HttpServletRequest request
    ) {
        return http.get()
                .uri("/allowed-users/" + chatId)
                .header("Authorization", bearerToken(request))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<String>>() {});
    }

    @GetMapping("/get-chats/{userId}")
    public Mono<ResponseEntity<String>> getChats(
            @PathVariable String userId,
            HttpServletRequest request
    ) {
        System.out.println("Getting chats for user: " + userId);
        return http.get()
                .uri("/get-chats/{userId}", userId)
                .header("Authorization", bearerToken(request))
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .doOnNext(body -> System.out.println("Error from chat service: " + body))
                                .flatMap(body -> Mono.error(new RuntimeException("Chat service error: " + body)))
                )
                .toEntity(String.class);
    }

    @PostMapping("/create-chat")
    public Mono<ResponseEntity<String>> createChat(
            @RequestBody ChatDTO chatDTO,
            HttpServletRequest request
    ) {
        return http.post()
                .uri("/create-chat")
                .header("Authorization", bearerToken(request))
                .bodyValue(chatDTO)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/get-chat-title/{chatId}")
    public Mono<ResponseEntity<String>> getChatTitle(
            @PathVariable String chatId,
            HttpServletRequest request
    ) {
        return http.get()
                .uri("/get-chat-title/{chatId}", chatId)
                .header("Authorization", bearerToken(request))
                .retrieve()
                .toEntity(String.class);
    }

    @PutMapping("/update-title")
    public Mono<ResponseEntity<String>> updateChatTitle(
            @RequestBody TitleDTO titleDTO,
            HttpServletRequest request
    ) {
        return http.put()
                .uri("/update-title")
                .header("Authorization", bearerToken(request))
                .bodyValue(titleDTO)
                .retrieve()
                .toEntity(String.class);
    }

    @DeleteMapping("/delete-user-from-chat")
    public Mono<ResponseEntity<Boolean>> deleteUserFromChat(
            @RequestBody AlterUserDTO alterUserDTO,
            HttpServletRequest request
    ) {
        return http.put()
                .uri("/delete-user-from-chat")
                .header("Authorization", bearerToken(request))
                .bodyValue(alterUserDTO)
                .retrieve()
                .toEntity(Boolean.class);
    }

    @PostMapping("/add-user-to-chat")
    public Mono<ResponseEntity<Boolean>> addUserToChat(
            @RequestBody AlterUserDTO alterUserDTO,
            HttpServletRequest request
    ) {
        return http.put()
                .uri("/add-user-to-chat")
                .header("Authorization", bearerToken(request))
                .bodyValue(alterUserDTO)
                .retrieve()
                .toEntity(Boolean.class);
    }
}