package com.userconnectionservice.repository;

import com.userconnectionservice.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // Find the follow relationship between two users
    Optional<Follow> findByFollowerAndFollowing(UUID follower, UUID following);

    // Check if a follow relationship exists
    boolean existsByFollowerAndFollowing(UUID follower, UUID following);

    // Get all users that this user is following
    List<Follow> findByFollower(UUID follower);

    // Get all followers of a user (users who follow this user)
    List<Follow> findByFollowing(UUID following);
}
