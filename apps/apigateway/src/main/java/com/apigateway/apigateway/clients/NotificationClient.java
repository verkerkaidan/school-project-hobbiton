package com.apigateway.apigateway.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client for the notification-service.
 *
 * All methods that forward the {@code Authorization} header pass the raw
 * {@code "Bearer <token>"} string so the notification-service can validate
 * the JWT and enforce its own access rules.
 */
@FeignClient(name = "notification-service", url = "${notification.service.url}")
public interface NotificationClient {

    @PostMapping("/notifications")
    void createNotification(@RequestBody Map<String, Object> payload);

    @PostMapping("/notifications/batch")
    void batchCreateNotifications(@RequestBody Map<String, Object> payload);

    /**
     * Deletes all notifications where the authenticated user is the recipient
     * or the actor. Intended for self-service account deletion.
     *
     * Constraints:
     *   The {@code authorization} parameter must be a valid {@code "Bearer <jwt>"} string.
     *   The notification-service derives the target user from the token itself;
     *       no userId path variable is accepted on this endpoint.
     *
     * Example:
     *   notificationClient.deleteUserNotifications(request.getHeader("Authorization"));
     *   // → { "deleted": 12 }
     *
     * @param authorization the raw {@code Authorization} header value ({@code "Bearer <jwt>"})
     * @return a map containing {@code "deleted"} with the number of rows removed
     */
    @DeleteMapping("/notifications/users/me")
    Map<String, Long> deleteUserNotifications(@RequestHeader("Authorization") String authorization);

    /**
     * Admin-only: deletes all notifications where the given user is the recipient
     * or the actor. Intended for admin-driven account deletion flows.
     *
     * Constraints:
     *   The {@code authorization} parameter must be a valid {@code "Bearer <jwt>"} string.
     *   The token must carry {@code ROLE_ADMIN} in its {@code role} claim array;
     *       the notification-service returns 403 otherwise.
     *   The target user is identified by the {@code userId} path variable, not the token.
     *
     * Example:
     *   notificationClient.adminDeleteUserNotifications(
     *       request.getHeader("Authorization"),
     *       UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
     *   );
     *   // → { "deleted": 7 }
     *
     * @param authorization the raw {@code Authorization} header value ({@code "Bearer <jwt>"})
     * @param userId        UUID of the user whose notifications should be deleted
     * @return a map containing {@code "deleted"} with the number of rows removed
     */
    @DeleteMapping("/notifications/users/{userId}")
    Map<String, Long> adminDeleteUserNotifications(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("userId") UUID userId);
}
