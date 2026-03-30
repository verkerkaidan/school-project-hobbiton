package com.interactionservice.service;

import com.interactionservice.dao.CommentDAO;
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
        return commentDAO.deleteComment(id);
    }

    public List<Comment> getAllComments(UUID referenceId) {
        return commentDAO.getAllComments(referenceId);
    }
}
