package com.interactionservice.service;

import com.interactionservice.dao.CommentDAO;
import com.interactionservice.dao.LikeDAO;
import com.interactionservice.dto.CommentDTO;
import com.interactionservice.events.CommentDeletedEvent;
import com.interactionservice.model.Comment;
import com.interactionservice.model.CustomLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentDAO commentDAO;
    @Mock
    private LikeDAO likeDAO;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("createComment returns the saved Comment object")
    void createComment_returnsSavedComment() {
        // Arrange
        UUID referenceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String commentText = "Test comment";

        CommentDTO dto = new CommentDTO();
        dto.referenceId = referenceId;
        dto.userId = userId;
        dto.comment = commentText;

        Comment savedComment = new Comment(referenceId, userId, commentText);
        savedComment.setId(UUID.randomUUID()); // simulate DB-generated ID

        when(commentDAO.createComment(any(Comment.class))).thenReturn(savedComment);

        // Act
        Comment result = commentService.createComment(dto);

        // Assert
        assertNotNull(result);
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(referenceId, result.getReferenceId());
        assertEquals(userId, result.getUserId());
        assertEquals(commentText, result.getComment());

        // Verify interaction
        verify(commentDAO).createComment(any(Comment.class));
        verifyNoMoreInteractions(commentDAO);
    }

    @Test
    @DisplayName("deleteComment deletes comment, removes likes, publishes event, and returns commentId")
    void deleteComment_deletesComment_removesLikes_publishesEvent_andReturnsId() {
        // Arrange
        UUID commentId = UUID.randomUUID();

        when(commentDAO.deleteComment(commentId)).thenReturn(commentId);

        // Act
        UUID result = commentService.deleteComment(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result);

        // Verify DAO interactions
        verify(commentDAO).deleteComment(commentId);
        verify(likeDAO).deleteAllByReference(commentId);

        // ✅ Clean and correct verification
        verify(eventPublisher).publishEvent(new CommentDeletedEvent(commentId));

        // Ensure no unexpected interactions
        verifyNoMoreInteractions(commentDAO, likeDAO, eventPublisher);
    }
}
