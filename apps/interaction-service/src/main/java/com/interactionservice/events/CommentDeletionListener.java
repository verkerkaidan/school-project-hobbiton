package com.interactionservice.events;

import com.interactionservice.model.Comment;
import com.interactionservice.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentDeletionListener {
    @Autowired
    private CommentService commentService;

    @EventListener
    public void handle(CommentDeletedEvent deletedEvent) {
        List<Comment> replies = commentService.getAllComments(deletedEvent.commentId());

        for (Comment reply : replies) {
            commentService.deleteComment(reply.getId());
        }
    }
}
