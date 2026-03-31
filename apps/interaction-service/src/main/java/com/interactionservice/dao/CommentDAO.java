package com.interactionservice.dao;

import com.interactionservice.model.Comment;
import com.interactionservice.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CommentDAO {
    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    /**
     * Retrieve Comment object and delete it
     *
     * @param id, identifier of the comment to be deleted
     * @return UUID of the deleted Comment Object
     */
    public UUID deleteComment(UUID id) {
        Comment commentObject = this.getComment(id);
        commentRepository.delete(commentObject);
        return commentObject.getId();
    }

    /**
     * Retrieve Comment object by identifier
     * Throws error if the Comment object was not found
     *
     * @param id, identifier of the comment to be deleted
     * @return Comment object
     */
    public Comment getComment(UUID id) {
        Comment comment = commentRepository.getCommentByIdIs(id).orElse(null);
        if (comment.equals(null)) {
            throw new RuntimeException("Comment object not found");
        } else {
            return comment;
        }
    }

    public List<Comment> getAllComments(UUID referenceId) {
        return commentRepository.getAllByReferenceId(referenceId);
    }

    public void deleteCommentsByReference(UUID referenceId) {
        commentRepository.deleteAllByReferenceId(referenceId);
    }

    public List<Comment> retrieveCommentsByUser(UUID userId) {
        return commentRepository.findByUserId(userId);
    }
}
