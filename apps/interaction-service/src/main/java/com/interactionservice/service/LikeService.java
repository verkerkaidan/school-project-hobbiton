package com.interactionservice.service;

import com.interactionservice.dao.LikeDAO;
import com.interactionservice.model.CustomLike;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private LikeDAO likeDAO;

    /**
     * Toggle the like state.
     * If it exists, it's deleted, if it doesn't it's created
     *
     * @param userId, identifier of the user that liked/removed a like
     * @param referenceId, identifier of the post or comment that was liked/got a like removed
     * @return true if the like now exists, or false if the like was removed
     */
    @Transactional
    public boolean toggleLike(UUID userId, UUID referenceId) {
        if (likeDAO.doesExist(userId, referenceId)) {
            likeDAO.deleteLike(userId, referenceId);
            logger.info("Like removed by user {} on reference {}", userId, referenceId);
            return false;
        }
        else {
            likeDAO.saveLike(new CustomLike(referenceId, userId));
            logger.info("Like added by user {} on reference {}", userId, referenceId);
            return true;
        }
    }

    /**
     * Checks if the user has liked the post or comment
     *
     * @param userId, uuid of the user
     * @param referenceId, uuid of the post or comment
     * @return true if the like exists, false if not
     */
    public boolean doesLikeExist(UUID userId, UUID referenceId) {
        return likeDAO.doesExist(userId, referenceId);
    }

    public Integer countLikesOfReference(UUID referenceId) {
        return likeDAO.countLikes(referenceId);
    }

    /**
     * Retrieves a list of likes that are connected to a given post or comment id
     * Maps that to a list of user identifiers
     *
     * @param referenceId, identifier of the post or comment
     * @return list of user identifiers that are connected via like to the reference
     */
    public List<UUID> getUsersThatLiked(UUID referenceId) {
        List<UUID> usersList = likeDAO.getLikesByReference(referenceId)
                .stream().map(
                        (like -> like.getUserId())
                ).toList();
        logger.info("Reference {} has {} users that liked it", referenceId, usersList.size());
        return usersList;
    }

    @Transactional
    public void deleteAllByReference(UUID referenceId) {
        likeDAO.deleteAllByReference(referenceId);
        logger.warn("All likes deleted for reference {}", referenceId);
    }

    @Transactional
    public void deleteAllByUser(UUID userId) {
        likeDAO.deleteAllByUser(userId);
        logger.warn("All likes deleted for user {}", userId);
    }
}