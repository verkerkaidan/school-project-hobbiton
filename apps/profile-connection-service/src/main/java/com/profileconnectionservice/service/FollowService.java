package com.profileconnectionservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.profileconnectionservice.model.Follow;
import com.profileconnectionservice.repository.FollowRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    /**
     * Toggles follow relationship between two profiles.
     * If the follow relationship exists, it will be removed (unfollow).
     * If it doesn't exist, a new follow relationship will be created.
     * 
     * @param currentProfileId the profile performing the follow/unfollow action
     * @param targetProfileId  the profile to follow/unfollow
     * @return true if now following, false if now unfollowed
     */
    public boolean toggleFollow(UUID currentProfileId, UUID targetProfileId) {
        if (currentProfileId.equals(targetProfileId)) {
            throw new IllegalArgumentException("A profile cannot follow themselves");
        }

        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(currentProfileId, targetProfileId);

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            return false;
        } else {
            Follow follow = new Follow(currentProfileId, targetProfileId);
            followRepository.save(follow);
            return true;
        }
    }

    /**
     * Check if a profile is following another profile.
     * 
     * @param currentProfileId the profile performing the follow check
     * @param targetProfileId  the profile to check if being followed
     * @return true if currentProfileId is following targetProfileId
     */
    public boolean isFollowing(UUID currentProfileId, UUID targetProfileId) {
        return followRepository.existsByFollowerAndFollowing(currentProfileId, targetProfileId);
    }

    /**
     * Get all followers of a profile.
     * 
     * @param profileId the profile whose followers to retrieve
     * @return Set of profile IDs who follow this profile
     */
    public Set<UUID> getFollowers(UUID profileId) {
        return followRepository.findByFollowing(profileId)
                .stream()
                .map(Follow::getFollower)
                .collect(Collectors.toSet());
    }

    /**
     * Get all profiles that this profile is following.
     * 
     * @param profileId the profile whose following list to retrieve
     * @return Set of profile IDs that this profile follows
     */
    public Set<UUID> getFollowing(UUID profileId) {
        return followRepository.findByFollower(profileId)
                .stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toSet());
    }
}
