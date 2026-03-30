package com.interactionservice.repository;

import com.interactionservice.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    // Check if the like exists
    boolean existsByReferenceIdAndUser_id(UUID referenceId, UUID userId);

    // Retrieve like by user and reference
    Like getLikeByReferenceIdAndUser_id(UUID referenceID, UUID userID);

    // Count the number of likes from a post or comment
    Integer countByReferenceId(UUID referenceId);

    // Get all likes for a given reference
    List<Like> getLikesByReferenceId(UUID referenceId);
}
