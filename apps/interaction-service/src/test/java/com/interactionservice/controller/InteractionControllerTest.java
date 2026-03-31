package com.interactionservice.controller;

import com.interactionservice.dto.CommentDTO;
import com.interactionservice.dto.InteractionReturnDTO;
import com.interactionservice.model.Comment;
import com.interactionservice.service.CommentService;
import com.interactionservice.service.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InteractionControllerTest {
    @InjectMocks
    private InteractionController interactionController;

    @Mock
    private CommentService commentService;

    @Mock
    private LikeService likeService;

    @Test
    @DisplayName("Toggling like returns correct boolean state")
    void toggleLike_returnsCorrectBoolean() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        when(likeService.toggleLike(userId, referenceId)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = interactionController.toggleLike(userId, referenceId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        Boolean result = response.getBody();
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    @DisplayName("Retrieving likes and comments for a reference returns correct DTO")
    void getLikesFromReference_returnsCorrectDTO() {
        // Arrange
        UUID referenceId = UUID.randomUUID();

        int likeCount = 5;

        Comment comment1 = new Comment(/* initialize as needed */);
        Comment comment2 = new Comment(/* initialize as needed */);
        List<Comment> comments = List.of(comment1, comment2);

        when(likeService.countLikesOfReference(referenceId)).thenReturn(likeCount);
        when(commentService.getAllComments(referenceId)).thenReturn(comments);

        // Act
        ResponseEntity<InteractionReturnDTO> response =
                interactionController.getLikesFromReference(referenceId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        InteractionReturnDTO dto = response.getBody();
        assertNotNull(dto);

        assertEquals(likeCount, dto.likes);
        assertEquals(comments, dto.comments);

        // Verify service interaction (without times)
        verify(likeService).countLikesOfReference(referenceId);
        verify(commentService).getAllComments(referenceId);
        verifyNoMoreInteractions(likeService, commentService);
    }

    @Test
    @DisplayName("Creating a comment returns the generated UUID")
    void createComment_returnsCommentId() {
        // Arrange
        UUID generatedId = UUID.randomUUID();

        CommentDTO inputDto = new CommentDTO();
        inputDto.comment = "Test comment";

        Comment createdComment = new Comment();
        createdComment.setId(generatedId);

        when(commentService.createComment(inputDto)).thenReturn(createdComment);

        // Act
        ResponseEntity<UUID> response = interactionController.createComment(inputDto);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        UUID responseId = response.getBody();
        assertNotNull(responseId);
        assertEquals(generatedId, responseId);

        // Verify service interaction
        verify(commentService).createComment(inputDto);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("Deleting a comment returns the deleted comment UUID")
    void deleteComment_returnsDeletedCommentId() {
        // Arrange
        UUID commentId = UUID.randomUUID();

        when(commentService.deleteComment(commentId)).thenReturn(commentId);

        // Act
        ResponseEntity<UUID> response =
                interactionController.deleteComment(commentId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        UUID responseId = response.getBody();
        assertNotNull(responseId);
        assertEquals(commentId, responseId);

        // Verify service interaction
        verify(commentService).deleteComment(commentId);
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("Retrieving users that liked a reference returns correct UUID list")
    void getUsersThatLiked_returnsCorrectList() {
        // Arrange
        UUID referenceId = UUID.randomUUID();

        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        List<UUID> users = List.of(user1, user2);

        when(likeService.getUsersThatLiked(referenceId)).thenReturn(users);

        // Act
        ResponseEntity<List<UUID>> response =
                interactionController.getUsersThatLiked(referenceId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        List<UUID> responseList = response.getBody();
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(users, responseList);

        // Verify service interaction
        verify(likeService).getUsersThatLiked(referenceId);
        verifyNoMoreInteractions(likeService);
    }

    @Test
    @DisplayName("Deleting a user removes all likes and comments and returns userId")
    void deleteUser_removesInteractionsAndReturnsUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        ResponseEntity<UUID> response =
                interactionController.deleteUser(userId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        UUID responseId = response.getBody();
        assertNotNull(responseId);
        assertEquals(userId, responseId);

        // Verify service interactions
        verify(likeService).deleteAllByUser(userId);
        verify(commentService).deleteAllByUser(userId);
        verifyNoMoreInteractions(likeService, commentService);
    }

    @Test
    @DisplayName("Deleting a post removes all likes and comments and returns postId")
    void deletePost_removesInteractionsAndReturnsPostId() {
        // Arrange
        UUID postId = UUID.randomUUID();

        // Act
        ResponseEntity<UUID> response =
                interactionController.deletePost(postId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        UUID responseId = response.getBody();
        assertNotNull(responseId);
        assertEquals(postId, responseId);

        // Verify service interactions
        verify(likeService).deleteAllByReference(postId);
        verify(commentService).deleteAllByReference(postId);
        verifyNoMoreInteractions(likeService, commentService);
    }
}
