package com.feedservice.controller;

import com.feedservice.model.UserFeed;
import com.feedservice.service.UserFeedService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/feed")
public class UserFeedController {
    private UserFeedService userFeedService;

    public UserFeedController(UserFeedService userFeedService) {
        this.userFeedService = userFeedService;
    }

    /**
     * Get all posts for a specific user
     *
     * @param userId the user ID
     * @return Set of posts
     */
    @GetMapping("/user/{userId}")
    public Set<UserFeed> getUserFeedPosts(@PathVariable UUID userId) {
        return this.userFeedService.getUserFeedPosts(userId);
    }

    /**
     * Get a single post by ID
     *
     * @param userFeedId the user feed ID
     * @return the UserFeed object
     */
    @GetMapping("/post/{userFeedId}")
    public UserFeed getOneUserFeedPost(@PathVariable UUID userFeedId) {
        return this.userFeedService.getUserFeedPostById(userFeedId);
    }

    /**
     * Get all posts from the database
     *
     * @return Set of all posts
     */
    @GetMapping("/all")
    public Set<UserFeed> getAllUserFeedPosts() {
        return this.userFeedService.getAllUserFeedPosts();
    }
}