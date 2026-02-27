package com.userconnectionservice.repository;

import com.userconnectionservice.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    // Find the follow relationship between two users
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    // Check if a follow relationship exists
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    // Get all users that this user is following
    List<Follow> findByFollowerId(Long followerId);
    
    // Get all followers of a user (users who follow this user)
    List<Follow> findByFollowingId(Long followingId);
}
