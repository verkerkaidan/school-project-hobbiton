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
import com.profileconnectionservice.model.Follow;
import com.profileconnectionservice.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceTests {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private FollowService followService;

    private UUID followerId;
    private UUID followingId;

    @BeforeEach
    void setUp() {
        followerId = TestFixtures.PROFILE_ID_1;
        followingId = TestFixtures.PROFILE_ID_2;
    }

    // ========== toggleFollow Tests ==========

    @Test
    void toggleFollow_WhenFollowDoesNotExist_CreatesFollowAndReturnsTrue() {
        // Arrange
        when(followRepository.findByFollowerAndFollowing(followerId, followingId))
                .thenReturn(Optional.empty());

        // Act
        boolean result = followService.toggleFollow(followerId, followingId);

        // Assert
        assertTrue(result);
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    void toggleFollow_WhenFollowExists_DeletesFollowAndReturnsFalse() {
        // Arrange
        Follow existingFollow = TestFixtures.createFollow(followerId, followingId);
        when(followRepository.findByFollowerAndFollowing(followerId, followingId))
                .thenReturn(Optional.of(existingFollow));

        // Act
        boolean result = followService.toggleFollow(followerId, followingId);

        // Assert
        assertFalse(result);
        verify(followRepository, times(1)).delete(existingFollow);
    }

    @Test
    void toggleFollow_WithSameProfile_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            followService.toggleFollow(followerId, followerId);
        });
        verify(followRepository, never()).save(any());
    }

    // ========== isFollowing Tests ==========

    @Test
    void isFollowing_WhenFollowingRelationExists_ReturnsTrue() {
        // Arrange
        when(followRepository.existsByFollowerAndFollowing(followerId, followingId))
                .thenReturn(true);

        // Act
        boolean result = followService.isFollowing(followerId, followingId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isFollowing_WhenFollowingRelationDoesNotExist_ReturnsFalse() {
        // Arrange
        when(followRepository.existsByFollowerAndFollowing(followerId, followingId))
                .thenReturn(false);

        // Act
        boolean result = followService.isFollowing(followerId, followingId);

        // Assert
        assertFalse(result);
    }

    // ========== getFollowers Tests ==========

    @Test
    void getFollowers_ReturnsSetOfFollowerProfileIds() {
        // Arrange
        UUID followerId1 = TestFixtures.PROFILE_ID_3;
        UUID followerId2 = TestFixtures.PROFILE_ID_4;
        Follow follow1 = TestFixtures.createFollow(followerId1, followingId);
        Follow follow2 = TestFixtures.createFollow(followerId2, followingId);

        when(followRepository.findByFollowing(followingId))
                .thenReturn(List.of(follow1, follow2));

        // Act
        Set<UUID> result = followService.getFollowers(followingId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(followerId1));
        assertTrue(result.contains(followerId2));
    }

    // ========== getFollowing Tests ==========

    @Test
    void getFollowing_ReturnsSetOfFollowingProfileIds() {
        // Arrange
        UUID followingId1 = TestFixtures.PROFILE_ID_2;
        UUID followingId2 = TestFixtures.PROFILE_ID_3;
        Follow follow1 = TestFixtures.createFollow(followerId, followingId1);
        Follow follow2 = TestFixtures.createFollow(followerId, followingId2);

        when(followRepository.findByFollower(followerId))
                .thenReturn(List.of(follow1, follow2));

        // Act
        Set<UUID> result = followService.getFollowing(followerId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(followingId1));
        assertTrue(result.contains(followingId2));
    }

    // ========== deleteAllUserData Tests ==========

    @Test
    void deleteAllUserData_WithValidUser_DeletesFollowsAndReturnsCount() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        int deletedAsInitiator = 7;
        int deletedAsTarget = 3;
        when(followRepository.deleteByInitiator(userId)).thenReturn(deletedAsInitiator);
        when(followRepository.deleteByTarget(userId)).thenReturn(deletedAsTarget);

        // Act
        int result = followService.deleteAllUserData(userId);

        // Assert
        assertEquals(10, result);
        verify(followRepository, times(1)).deleteByInitiator(userId);
        verify(followRepository, times(1)).deleteByTarget(userId);
    }

    @Test
    void deleteAllUserData_WithFollowerOnly_ReturnsOnlyFollowerCount() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        when(followRepository.deleteByInitiator(userId)).thenReturn(5);
        when(followRepository.deleteByTarget(userId)).thenReturn(0);

        // Act
        int result = followService.deleteAllUserData(userId);

        // Assert
        assertEquals(5, result);
        verify(followRepository, times(1)).deleteByInitiator(userId);
        verify(followRepository, times(1)).deleteByTarget(userId);
    }

    @Test
    void deleteAllUserData_WithFollowingOnly_ReturnsOnlyFollowingCount() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        when(followRepository.deleteByInitiator(userId)).thenReturn(0);
        when(followRepository.deleteByTarget(userId)).thenReturn(8);

        // Act
        int result = followService.deleteAllUserData(userId);

        // Assert
        assertEquals(8, result);
        verify(followRepository, times(1)).deleteByInitiator(userId);
        verify(followRepository, times(1)).deleteByTarget(userId);
    }

}
