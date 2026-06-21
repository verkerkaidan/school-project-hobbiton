package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.*;
import com.apigateway.apigateway.enums.UserDeleteOrigin;
import com.apigateway.apigateway.models.Post;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PostClient postClient;
    private final MediaClient mediaClient;
    private final InteractionClient interactionClient;
    private final ProfileConnectionClient profileConnectionClient;
    private final ChatClient chatClient;
    private final NotificationClient notificationClient;
    private final LibraryClient libraryClient;
    private final ProfileClient profileClient;
    private final AuthClient authClient;

    public UserService(
            MediaClient mediaClient,
            PostClient postClient,
            InteractionClient interactionClient,
            ProfileConnectionClient profileConnectionClient,
            ChatClient chatClient,
            NotificationClient notificationClient,
            LibraryClient libraryClient,
            ProfileClient profileClient, AuthClient authClient) {
        this.mediaClient = mediaClient;
        this.postClient = postClient;
        this.interactionClient = interactionClient;
        this.profileConnectionClient = profileConnectionClient;
        this.chatClient = chatClient;
        this.notificationClient = notificationClient;
        this.libraryClient = libraryClient;
        this.profileClient = profileClient;
        this.authClient = authClient;
    }

    /**
     * Authentication and profile should be deleted last, as those are our reference to the other services
     * If auth exists without profile, it causes problems
     * Each other service can have their user deleted without causing problems so the check isn't needed individually
     * @param headers, request headers for library client
     * @param userId, UUID of the user to be deleted
     */
    public boolean deleteUser(UUID userId, Map<String, String> headers, UserDeleteOrigin origin) {
        deleteAllUserPosts(userId);
        deleteAllUserMediaAndReports(userId);
        deleteUserInteraction(userId);
        deleteUserConnections(userId);
        deleteUserFromChats(userId);
        deleteUserLists(headers, userId);

        switch (origin) {
            case USER -> deleteUserOwnNotifications(headers);
            case ADMIN -> deleteUserAdminNotifications(headers, userId);
            case null, default -> throw new RuntimeException(
                    "[USERSERVICE deleteUser] Not a valid call with UserDeleteOrigin: " + origin.name()
            );
        }

        if (deleteUserProfile(userId)) {
            return deleteUserAuth(userId);
        }
        else {
            return false;
        }
    }

    private boolean deleteAllUserPosts(UUID userId) {
        try {
            List<Post> posts = postClient.getUserPosts(userId);
            List<UUID> uuids;
            if (!posts.isEmpty()) {
                uuids = posts.stream().map(Post::getId).toList();
                try {
                    interactionClient.deleteAllByReferences(uuids);
                } catch (Exception e) {
                    logger.error("[USERSERVICE deleteAllUserPosts] Interaction service could not be reached --- \n{}", e.getMessage());
                }
                postClient.deleteAllUserPosts(userId);
            }
            return true;
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteAllUserPosts] Post service could not be reached --- \n{}", e.getMessage());
            return false;
        }
    }

    private boolean deleteAllUserMediaAndReports(UUID userId) {
        try {
            mediaClient.deleteUser(userId);
            return true;
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteAllUserMediaAndReports] Media service could not be reached --- \n{}", e.getMessage());
            return false;
        }
    }

    private boolean deleteUserInteraction(UUID userId) {
        try {
            interactionClient.deleteUser(userId);
            return true;
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserInteraction] Interaction service could not be reached --- \n{}", e.getMessage());
            return false;
        }
    }

    private boolean deleteUserConnections(UUID userId) {
        try {
            profileConnectionClient.deleteUserBlocks(userId);
            profileConnectionClient.deleteUserFollows(userId);
            profileConnectionClient.deleteUserReports(userId);
            return true;
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserConnections] ProfileConnection service could not be reached --- \n{}", e.getMessage());
            return false;
        }
    }

    private boolean deleteUserFromChats(UUID userId) {
        try {
            chatClient.deleteUserFromAllChats(userId.toString());
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserFromChats] Chat service could not be reached --- \n{}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean deleteUserOwnNotifications(Map<String, String> headers) {
        try {
            notificationClient.deleteUserNotifications(headers.get("Authorization"));
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserOwnNotifications] notification service could not be reached --- \n{}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean deleteUserAdminNotifications(Map<String, String> headers, UUID userId) {
        try {
            notificationClient.adminDeleteUserNotifications(headers.get("Authorization"), userId);
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserAdminNotifications] notification service could not be reached --- \n{}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean deleteUserLists(Map<String, String> headers, UUID userId) {
        try {
            libraryClient.deleteUserLists(headers, userId);
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserLists] library service could not be reached --- \n{}", e.getMessage());
            return false;
        }
        return true;
    }

    private boolean deleteUserAuth(UUID userId) {
        try {
            authClient.deleteAuthentication(userId);
            return true;
        } catch (Exception e) {
            logger.error("[USERSERVICE deleteUserAuth] Authentication service could not be reached --- \n{}", e.getMessage());
            throw new RuntimeException("Responsible service could not be reached");
        }
    }

    private boolean deleteUserProfile(UUID userId) {
        try {
            profileClient.deleteProfile(userId);
            return true;
        } catch (FeignException e) {
            logger.warn("[USERSERVICE deleteUserProfile] Profile could not be found --- \n{}", e.getMessage());
            return true;
        }
        catch (Exception e) {
            logger.error("[USERSERVICE deleteUserProfile] Profile service could not be reached --- \n{}", e.getMessage());
            throw new RuntimeException("Responsible service could not be reached");
        }
    }
}
