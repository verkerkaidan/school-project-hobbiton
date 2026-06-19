package com.interactionservice.service;

import com.interactionservice.events.CommentDeletedEvent;
import com.interactionservice.dao.CommentDAO;
import com.interactionservice.dao.LikeDAO;
import com.interactionservice.dto.CommentDTO;
import com.interactionservice.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private LikeDAO likeDAO;

    /**
     * Creating a Comment from the dto and sending to DAO
     *
     * @param commentDTO, Object with userId, referenceId and the commentContent
     * @return the created Comment object
     */
    public Comment createComment(CommentDTO commentDTO) {
        Comment commentObject = new Comment(
                commentDTO.referenceId,
                commentDTO.userId,
                commentDTO.comment
        );
        Comment created = commentDAO.createComment(commentObject);
        logger.info("Comment '{}' created by user {} on reference {}", created.getComment(), created.getUserId(), created.getReferenceId());
        return created;
    }

    /**
     * EventEmitter comment delete
     * Deletes comment and publishes event so each child comment is deleted
     *
     * @param commentId
     * @return
     */
    @Transactional
    public UUID deleteComment(UUID commentId) {
        commentDAO.deleteComment(commentId);
        likeDAO.deleteAllByReference(commentId);
        eventPublisher.publishEvent(new CommentDeletedEvent(commentId));
        logger.info("Comment {} deleted and CommentDeletedEvent published", commentId);
        return commentId;
    }

    public List<Comment> getAllComments(UUID referenceId) {
        return commentDAO.getAllComments(referenceId);
    }

    /**
     * Deletes all comments that are connected to a given post
     *
     * @param postIds, identifiers of the posts to be deleted
     */
    @Transactional
    public void deleteAllByReferences(List<UUID> postIds) {
        List<Comment> comments = commentDAO.getAllComments(postIds);
        for (Comment comment : comments) {
            deleteComment(comment.getId());
        }
        logger.warn("All {} comments deleted for references {}", comments.size(), postIds);
    }

    /**
     * Retrieves all comments placed by the given user and deletes those
     *
     * @param userId, identifier of the user to be deleted
     */
    public void deleteAllByUser(UUID userId) {
        List<Comment> comments = commentDAO.retrieveCommentsByUser(userId);
        comments.forEach(comment -> deleteComment(comment.getId()));
        logger.warn("All {} comments deleted for user {}", comments.size(), userId);
    }
}