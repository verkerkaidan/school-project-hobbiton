package com.userconnectionservice.service;

import com.userconnectionservice.model.Follow;
import com.userconnectionservice.model.User;
import com.userconnectionservice.repository.FollowRepository;
import com.userconnectionservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowService {
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Toggles follow relationship between two users.
     * If the follow relationship exists, it will be removed (unfollow).
     * If it doesn't exist, a new follow relationship will be created.
     * 
     * @param currentUserId the user performing the follow/unfollow action
     * @param targetUserId the user to follow/unfollow
     * @return true if now following, false if now unfollowed
     */
    public boolean toggleFollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("A user cannot follow themselves");
        }
        
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("Current user not found"));
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Optional<Follow> existingFollow = followRepository.findFollowRelationship(currentUserId, targetUserId);
        
        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            return false;
        } else {
            Follow follow = new Follow(currentUser, targetUser);
            followRepository.save(follow);
            return true;
        }
    }
    
    /**
     * Check if a user is following another user.
     * 
     * @param currentUserId the user performing the follow check
     * @param targetUserId the user to check if being followed
     * @return true if currentUserId is following targetUserId
     */
    public boolean isFollowing(Long currentUserId, Long targetUserId) {
        return followRepository.followExists(currentUserId, targetUserId);
    }
    
    /**
     * Get all followers of a user.
     * 
     * @param userId the user whose followers to retrieve
     * @return Set of users who follow this user
     */
    public Set<User> getFollowers(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return followRepository.findAllFollowingByUserId(userId)
            .stream()
            .map(Follow::getFollower)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get all users that this user is following.
     * 
     * @param userId the user whose following list to retrieve
     * @return Set of users that this user follows
     */
    public Set<User> getFollowing(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return followRepository.findAllFollowersByUserId(userId)
            .stream()
            .map(Follow::getFollowing)
            .collect(Collectors.toSet());
    }
}
