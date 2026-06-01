package com.apigateway.apigateway.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.image.DataBuffer;
import java.net.URI;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

    @Value("${chat.ws-url}")
    private String wsBaseUrl;

    @Override
    public Mono<Void> handle(WebSocketSession browserSession) {
        // Pull chatId and userId from the URI query params
        var params = UriComponentsBuilder
                .fromUri(browserSession.getHandshakeInfo().getUri())
                .build()
                .getQueryParams();

        String chatId = extractPathSegment(browserSession);
        String userId = params.getFirst("userId");

        if (chatId == null || userId == null) {
            return browserSession.close(CloseStatus.BAD_DATA);
        }

        var dotnetUri = URI.create(wsBaseUrl + "/ws/" + chatId + "?userId=" + userId);

        // Connect to .NET, then wire the two sessions together bidirectionally
        return client.execute(dotnetUri, dotnetSession -> {
            // .NET → browser
            Flux<Void> inbound = dotnetSession.receive()
                    .flatMap(msg -> browserSession.send(Mono.just(msg)));

            // browser → .NET
            Flux<Void> outbound = browserSession.receive()
                    .flatMap(msg -> dotnetSession.send(Mono.just(msg)));
            return Mono.zip(inbound.then(), outbound.then()).then();
        });
    }

    private String extractPathSegment(WebSocketSession session) {
        // URI pattern is /ws/{chatId}
        String path = session.getHandshakeInfo().getUri().getPath();
        String[] parts = path.split("/");
        return parts.length >= 3 ? parts[2] : null;
    }
}