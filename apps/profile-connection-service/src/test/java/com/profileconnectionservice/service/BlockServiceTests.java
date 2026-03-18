package com.profileconnectionservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.profileconnectionservice.TestFixtures;
import com.profileconnectionservice.model.Block;
import com.profileconnectionservice.model.Follow;
import com.profileconnectionservice.repository.BlockRepository;
import com.profileconnectionservice.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
class BlockServiceTests {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private BlockService blockService;

    private UUID blockerId;
    private UUID blockedId;

    @BeforeEach
    void setUp() {
        blockerId = TestFixtures.PROFILE_ID_1;
        blockedId = TestFixtures.PROFILE_ID_2;
    }

    // ========== toggleBlock Tests ==========

    @Test
    void toggleBlock_WhenBlockDoesNotExist_CreatesBlockAndReturnsTrue() {
        // Arrange
        when(blockRepository.findByBlockerAndBlocked(blockerId, blockedId))
                .thenReturn(Optional.empty());
        when(followRepository.findByFollowerAndFollowing(blockerId, blockedId))
                .thenReturn(Optional.empty());
        when(followRepository.findByFollowerAndFollowing(blockedId, blockerId))
                .thenReturn(Optional.empty());

        // Act
        boolean result = blockService.toggleBlock(blockerId, blockedId);

        // Assert
        assertTrue(result);
        verify(blockRepository, times(1)).save(any(Block.class));
        verify(followRepository, times(2)).findByFollowerAndFollowing(any(UUID.class), any(UUID.class));
    }

    @Test
    void toggleBlock_WhenBlockExists_DeletesBlockAndReturnsFalse() {
        // Arrange
        Block existingBlock = TestFixtures.createBlock(blockerId, blockedId);
        when(blockRepository.findByBlockerAndBlocked(blockerId, blockedId))
                .thenReturn(Optional.of(existingBlock));

        // Act
        boolean result = blockService.toggleBlock(blockerId, blockedId);

        // Assert
        assertFalse(result);
        verify(blockRepository, times(1)).delete(existingBlock);
    }

    @Test
    void toggleBlock_WhenBlockingAndFollowRelationsExist_DeletesFollowRelations() {
        // Arrange
        UUID initiatorId = blockerId;
        UUID targetId = blockedId;
        Follow follow1 = TestFixtures.createFollow(initiatorId, targetId);
        Follow follow2 = TestFixtures.createFollow(targetId, initiatorId);

        when(blockRepository.findByBlockerAndBlocked(initiatorId, targetId))
                .thenReturn(Optional.empty());
        when(followRepository.findByFollowerAndFollowing(initiatorId, targetId))
                .thenReturn(Optional.of(follow1));
        when(followRepository.findByFollowerAndFollowing(targetId, initiatorId))
                .thenReturn(Optional.of(follow2));

        // Act
        blockService.toggleBlock(initiatorId, targetId);

        // Assert
        verify(followRepository, times(1)).delete(follow1);
        verify(followRepository, times(1)).delete(follow2);
    }

    @Test
    void toggleBlock_WithSameProfile_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            blockService.toggleBlock(blockerId, blockerId);
        });
        verify(blockRepository, never()).save(any());
    }

    // ========== isBlocking Tests ==========

    @Test
    void isBlocking_WhenBlockingRelationExists_ReturnsTrue() {
        // Arrange
        when(blockRepository.existsByBlockerAndBlocked(blockerId, blockedId))
                .thenReturn(true);

        // Act
        boolean result = blockService.isBlocking(blockerId, blockedId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isBlocking_WhenBlockingRelationDoesNotExist_ReturnsFalse() {
        // Arrange
        when(blockRepository.existsByBlockerAndBlocked(blockerId, blockedId))
                .thenReturn(false);

        // Act
        boolean result = blockService.isBlocking(blockerId, blockedId);

        // Assert
        assertFalse(result);
    }

    // ========== isBlockedBy Tests ==========

    @Test
    void isBlockedBy_WhenBlockedByRelationExists_ReturnsTrue() {
        // Arrange
        when(blockRepository.existsByBlockerAndBlocked(blockedId, blockerId))
                .thenReturn(true);

        // Act
        boolean result = blockService.isBlockedBy(blockerId, blockedId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isBlockedBy_WhenBlockedByRelationDoesNotExist_ReturnsFalse() {
        // Arrange
        when(blockRepository.existsByBlockerAndBlocked(blockedId, blockerId))
                .thenReturn(false);

        // Act
        boolean result = blockService.isBlockedBy(blockerId, blockedId);

        // Assert
        assertFalse(result);
    }

    // ========== getBlocked Tests ==========

    @Test
    void getBlocked_ReturnsSetOfBlockedProfileIds() {
        // Arrange
        UUID blockedId1 = TestFixtures.PROFILE_ID_2;
        UUID blockedId2 = TestFixtures.PROFILE_ID_3;
        Block block1 = TestFixtures.createBlock(blockerId, blockedId1);
        Block block2 = TestFixtures.createBlock(blockerId, blockedId2);

        when(blockRepository.findByBlocker(blockerId))
                .thenReturn(List.of(block1, block2));

        // Act
        Set<UUID> result = blockService.getBlocked(blockerId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(blockedId1));
        assertTrue(result.contains(blockedId2));
    }

}
