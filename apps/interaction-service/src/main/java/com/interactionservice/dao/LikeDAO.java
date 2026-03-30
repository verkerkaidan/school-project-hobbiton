package com.interactionservice.dao;

import com.interactionservice.model.Like;
import com.interactionservice.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LikeDAO {
    @Autowired
    private LikeRepository likeRepository;

    public boolean doesExist(UUID userId, UUID referenceId) {
        return likeRepository.existsByReferenceIdAndUser_id(userId, referenceId);
    }

    /**
     * Deletes a like from the database, by retrieving it via user and reference first
     *
     * @param userId, identifier of the user that removed their like
     * @param referenceId, identifier of the reference that had their like removed
     * @return UUID of the like that was deleted
     */
    public UUID deleteLike(UUID userId, UUID referenceId) {
        Like likeObject = this.getLikeByReferenceAndUser(userId, referenceId);
        likeRepository.delete(likeObject);
        return likeObject.getId();
    }

    public Like getLikeByReferenceAndUser(UUID userId, UUID referenceId) {
        return likeRepository.getLikeByReferenceIdAndUser_id(referenceId, userId);
    }

    public Like saveLike(Like likeObject) {
        return likeRepository.save(likeObject);
    }

    public Integer countLikes(UUID referenceID) {
        return likeRepository.countByReferenceId(referenceID);
    }

    public List<Like> getLikesByReference(UUID referenceId) {
        return likeRepository.getLikesByReferenceId(referenceId);
    }
}
