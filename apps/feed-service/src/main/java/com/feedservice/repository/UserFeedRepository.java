package com.feedservice.repository;

import com.feedservice.model.UserFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface UserFeedRepository extends JpaRepository<UserFeed, UUID> {
    Set<UserFeed> findByUserId(UUID userId);
}

