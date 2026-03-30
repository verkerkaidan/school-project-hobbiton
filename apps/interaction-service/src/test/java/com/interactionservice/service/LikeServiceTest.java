package com.interactionservice.service;

import com.interactionservice.dao.LikeDAO;
import com.interactionservice.model.CustomLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {
    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeDAO likeDAO;

    @Test
    @DisplayName("Toggling like removes existing like and returns false")
    void toggleLike_whenLikeExists_removesLikeAndReturnsFalse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        when(likeDAO.doesExist(userId, referenceId)).thenReturn(true);

        // Act
        boolean result = likeService.toggleLike(userId, referenceId);

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(likeDAO).doesExist(userId, referenceId);
        verify(likeDAO).deleteLike(userId, referenceId);
        verifyNoMoreInteractions(likeDAO);
    }

    @Test
    @DisplayName("getUsersThatLiked returns a list of user UUIDs who liked a reference")
    void getUsersThatLiked_returnsCorrectUserList() {
        // Arrange
        UUID referenceId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        CustomLike like1 = new CustomLike(referenceId, user1);
        CustomLike like2 = new CustomLike(referenceId, user2);

        when(likeDAO.getLikesByReference(referenceId)).thenReturn(List.of(like1, like2));

        // Act
        List<UUID> result = likeService.getUsersThatLiked(referenceId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));

        // Verify interaction
        verify(likeDAO).getLikesByReference(referenceId);
        verifyNoMoreInteractions(likeDAO);
    }
}
