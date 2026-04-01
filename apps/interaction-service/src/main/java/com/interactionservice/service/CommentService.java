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

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
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
        return commentDAO.createComment(commentObject);
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
        return commentId;
    }

    public List<Comment> getAllComments(UUID referenceId) {
        return commentDAO.getAllComments(referenceId);
    }

    /**
     * Deletes all comments that are connected to a given post
     *
     * @param postId, identifier of the post to be deleted
     */
    @Transactional
    public void deleteAllByReference(UUID postId) {
        List<Comment> comments = commentDAO.getAllComments(postId);
        for (Comment comment : comments) {
            deleteComment(comment.getId());
        }
    }

    /**
     * Retrieves all comments placed by the given user and deletes those
     *
     * @param userId, identifier of the user to be deleted
     */
    public void deleteAllByUser(UUID userId) {
        List<Comment> comments = commentDAO.retrieveCommentsByUser(userId);
        comments.forEach(comment -> deleteComment(comment.getId()));
    }
}
