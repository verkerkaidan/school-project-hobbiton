package com.userconnectionservice.service;

import com.userconnectionservice.model.Follow;
import com.userconnectionservice.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    /**
     * Toggles follow relationship between two users.
     * If the follow relationship exists, it will be removed (unfollow).
     * If it doesn't exist, a new follow relationship will be created.
     * 
     * @param currentUserId the user performing the follow/unfollow action
     * @param targetUserId  the user to follow/unfollow
     * @return true if now following, false if now unfollowed
     */
    public boolean toggleFollow(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("A user cannot follow themselves");
        }

        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(currentUserId, targetUserId);

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            return false;
        } else {
            Follow follow = new Follow(currentUserId, targetUserId);
            followRepository.save(follow);
            return true;
        }
    }

    /**
     * Check if a user is following another user.
     * 
     * @param currentUserId the user performing the follow check
     * @param targetUserId  the user to check if being followed
     * @return true if currentUserId is following targetUserId
     */
    public boolean isFollowing(UUID currentUserId, UUID targetUserId) {
        return followRepository.existsByFollowerAndFollowing(currentUserId, targetUserId);
    }

    /**
     * Get all followers of a user.
     * 
     * @param userId the user whose followers to retrieve
     * @return Set of user IDs who follow this user
     */
    public Set<UUID> getFollowers(UUID userId) {
        return followRepository.findByFollowing(userId)
                .stream()
                .map(Follow::getFollower)
                .collect(Collectors.toSet());
    }

    /**
     * Get all users that this user is following.
     * 
     * @param userId the user whose following list to retrieve
     * @return Set of user IDs that this user follows
     */
    public Set<UUID> getFollowing(UUID userId) {
        return followRepository.findByFollower(userId)
                .stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toSet());
    }
}
