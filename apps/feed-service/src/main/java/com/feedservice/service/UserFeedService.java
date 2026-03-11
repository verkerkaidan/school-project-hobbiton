package com.feedservice.service;

import com.feedservice.dao.UserFeedDAO;
import com.feedservice.model.UserFeed;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class UserFeedService {
    private UserFeedDAO userFeedDAO;

    public UserFeedService(UserFeedDAO userFeedDAO) {
        this.userFeedDAO = userFeedDAO;
    }

    /**
     * Fetch all posts for a specific user
     *
     * @param userId the user ID to fetch posts for
     * @return Set of posts for the user
     */
    public Set<UserFeed> getUserFeedPosts(UUID userId) {
        return this.userFeedDAO.getUserFeedPostsByUserId(userId);
    }

    /**
     * Fetch a single post by its ID
     *
     * @param userFeedId the user feed ID
     * @return the UserFeed object
     */
    public UserFeed getUserFeedPostById(UUID userFeedId) {
        return this.userFeedDAO.getUserFeedPostById(userFeedId);
    }

    /**
     * Fetch all posts from the database
     *
     * @return Set of all posts
     */
    public Set<UserFeed> getAllUserFeedPosts() {
        return this.userFeedDAO.getAllUserFeedPosts();
    }
}