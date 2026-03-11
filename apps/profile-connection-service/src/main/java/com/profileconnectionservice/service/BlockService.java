package com.profileconnectionservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.profileconnectionservice.model.Block;
import com.profileconnectionservice.repository.BlockRepository;
import com.profileconnectionservice.repository.FollowRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlockService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private FollowRepository followRepository;

    /**
     * Toggles block relationship between two profiles.
     * If the block relationship exists, it will be removed (unblock).
     * If it doesn't exist, a new block relationship will be created.
     * 
     * When blocking, all follow relationships between the two profiles are removed.
     * 
     * @param currentProfileId the profile performing the block/unblock action
     * @param targetProfileId  the profile to block/unblock
     * @return true if now blocking, false if now unblocked
     */
    public boolean toggleBlock(UUID currentProfileId, UUID targetProfileId) {
        if (currentProfileId.equals(targetProfileId)) {
            throw new IllegalArgumentException("A profile cannot block themselves");
        }

        Optional<Block> existingBlock = blockRepository.findByBlockerAndBlocked(currentProfileId, targetProfileId);

        if (existingBlock.isPresent()) {
            blockRepository.delete(existingBlock.get());
            return false;
        } else {
            // Remove any follow relationships between the two profiles
            followRepository.findByFollowerAndFollowing(currentProfileId, targetProfileId)
                    .ifPresent(followRepository::delete);
            followRepository.findByFollowerAndFollowing(targetProfileId, currentProfileId)
                    .ifPresent(followRepository::delete);

            Block block = new Block(currentProfileId, targetProfileId);
            blockRepository.save(block);
            return true;
        }
    }

    /**
     * Check if a profile is blocking another profile.
     * 
     * @param currentProfileId the profile performing the block check
     * @param targetProfileId  the profile to check if being blocked
     * @return true if currentProfileId is blocking targetProfileId
     */
    public boolean isBlocking(UUID currentProfileId, UUID targetProfileId) {
        return blockRepository.existsByBlockerAndBlocked(currentProfileId, targetProfileId);
    }

    /**
     * Check if a profile is blocked by another profile.
     * Internal function to check if currentProfileId is blocked by targetProfileId.
     * 
     * @param currentProfileId the profile to check if being blocked
     * @param targetProfileId  the profile who may have blocked currentProfileId
     * @return true if targetProfileId has blocked currentProfileId
     */
    public boolean isBlockedBy(UUID currentProfileId, UUID targetProfileId) {
        return blockRepository.existsByBlockerAndBlocked(targetProfileId, currentProfileId);
    }

    /**
     * Get all profiles that this profile has blocked.
     * 
     * @param profileId the profile whose blocked list to retrieve
     * @return Set of profile IDs that this profile has blocked
     */
    public Set<UUID> getBlocked(UUID profileId) {
        return blockRepository.findByBlocker(profileId)
                .stream()
                .map(Block::getBlocked)
                .collect(Collectors.toSet());
    }
}
