package com.profileconnectionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fire-and-forget HTTP client for the GoLang notification-service.
 * All calls are @Async so a notification failure never blocks the caller.
 */
@Slf4j
@Service
public class NotificationClient {

    private final RestClient notificationRestClient;
    private final RestClient profileRestClient;

    public NotificationClient(
            @Value("${notification.service.url}") String notificationServiceUrl,
            @Value("${profile.service.url}") String profileServiceUrl) {
        this.notificationRestClient = RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
        this.profileRestClient = RestClient.builder()
                .baseUrl(profileServiceUrl)
                .build();
    }

    /**
     * Fetches the username of the given user from the profile-service. Returns null
     * on any error.
     */
    private String fetchUsername(UUID userId) {
        try {
            Map<?, ?> profile = profileRestClient.get()
                    .uri("/api/profile/{id}", userId)
                    .retrieve()
                    .body(Map.class);
            if (profile != null && profile.get("username") instanceof String username) {
                return username;
            }
        } catch (Exception e) {
            log.warn("Could not fetch username for actor {}: {}", userId, e.getMessage());
        }
        return null;
    }

    /**
     * Sends a FOLLOW notification to the notification-service.
     *
     * @param targetUserId the user who was followed (recipient)
     * @param actorUserId  the user who performed the follow action
     */
    @Async
    public void sendFollowNotification(UUID targetUserId, UUID actorUserId) {
        try {
            String username = fetchUsername(actorUserId);
            String message = (username != null)
                    ? "@" + username + " started following you"
                    : "Someone started following you";

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", targetUserId.toString());
            payload.put("actorId", actorUserId.toString());
            payload.put("type", "FOLLOW");
            payload.put("message", message);

            notificationRestClient.post()
                    .uri("/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Follow notification sent: actor={} -> target={}", actorUserId, targetUserId);
        } catch (Exception e) {
            // Notification failures must not affect the follow operation
            log.warn("Failed to send follow notification (actor={}, target={}): {}",
                    actorUserId, targetUserId, e.getMessage());
        }
    }
}
