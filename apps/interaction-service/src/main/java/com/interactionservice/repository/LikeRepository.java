package com.interactionservice.repository;

import com.interactionservice.model.CustomLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<CustomLike, UUID> {
    // Check if the like exists
    boolean existsByReferenceIdAndUserId(UUID referenceId, UUID userId);

    // Retrieve like by user and reference
    CustomLike getCustomLikeByReferenceIdAndUserId(UUID referenceID, UUID userID);

    // Count the number of likes from a post or comment
    Integer countByReferenceId(UUID referenceId);

    // Get all likes for a given reference
    List<CustomLike> getCustomLikesByReferenceId(UUID referenceId);

    // Delete all likes that are connected to the given reference
    void deleteAllByReferenceId(UUID referenceId);

    // Delete all like that are connected to the given reference
    void deleteAllByUserId(UUID userId);
}
