package com.feedservice.dao;

import com.feedservice.model.UserFeed;
import com.feedservice.repository.UserFeedRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class UserFeedDAO {
    private UserFeedRepository userFeedRepository;

    public UserFeedDAO(UserFeedRepository userFeedRepository) {
        this.userFeedRepository = userFeedRepository;
    }

    public Set<UserFeed> getUserFeedPostsByUserId(UUID userId) {
        return this.userFeedRepository.findByUserId(userId);
    }

    public UserFeed getUserFeedPostById(UUID userFeedId) {
        Optional<UserFeed> optionalUserFeed = this.userFeedRepository.findById(userFeedId);

        if (optionalUserFeed.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatusCode.valueOf(404)
            );
        } else {
            return optionalUserFeed.get();
        }
    }

    public Set<UserFeed> getAllUserFeedPosts() {
        return (Set<UserFeed>) this.userFeedRepository.findAll();
    }
}


