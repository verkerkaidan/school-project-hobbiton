package com.interactionservice.service;

import com.interactionservice.dao.CommentDAO;
import com.interactionservice.dao.LikeDAO;
import com.interactionservice.dto.CommentDTO;
import com.interactionservice.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    @Autowired
    private CommentDAO commentDAO;

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

    @Transactional
    public UUID deleteComment(UUID id) {
        likeDAO.deleteAllByReference(id);
        this.deleteAllByReference(id);
        UUID commentId = commentDAO.deleteComment(id);
        return commentId;
    }

    public List<Comment> getAllComments(UUID referenceId) {
        return commentDAO.getAllComments(referenceId);
    }

    @Transactional
    public void deleteAllByReference(UUID postId) {
        commentDAO.deleteCommentsByReference(postId);
    }

    public void deleteAllByUser(UUID userId) {
        List<Comment> comments = commentDAO.retrieveCommentsByUser(userId);
        comments.forEach(comment -> deleteComment(comment.getId()));
    }
}
