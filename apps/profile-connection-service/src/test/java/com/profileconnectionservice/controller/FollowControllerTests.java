package com.profileconnectionservice.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.profileconnectionservice.TestFixtures;
import com.profileconnectionservice.config.TestSecurityConfiguration;
import com.profileconnectionservice.model.AuditLog;
import com.profileconnectionservice.service.AuditLogService;
import com.profileconnectionservice.service.FollowService;

@WebMvcTest(FollowController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class FollowControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private FollowService followService;

        @MockBean
        private AuditLogService auditLogService;

        private UUID currentProfileId;
        private UUID targetProfileId;

        @BeforeEach
        void setUp() {
                currentProfileId = TestFixtures.PROFILE_ID_1;
                targetProfileId = TestFixtures.PROFILE_ID_2;
        }

        // ========== toggleFollow Tests ==========

        @Test
        void toggleFollow_WhenFollowDoesNotExist_ReturnsTrue() throws Exception {
                when(followService.toggleFollow(currentProfileId, targetProfileId))
                                .thenReturn(true);

                mockMvc.perform(put("/follows/{targetProfileId}", targetProfileId)
                                .header("X-Profile-Id", currentProfileId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));
        }

        @Test
        void toggleFollow_WhenFollowExists_ReturnsFalse() throws Exception {
                when(followService.toggleFollow(currentProfileId, targetProfileId))
                                .thenReturn(false);

                mockMvc.perform(put("/follows/{targetProfileId}", targetProfileId)
                                .header("X-Profile-Id", currentProfileId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().string("false"));
        }

        // ========== getFollowers Tests ==========

        @Test
        void getFollowers_ReturnsFollowerProfileIds() throws Exception {
                UUID followerId1 = TestFixtures.PROFILE_ID_3;
                UUID followerId2 = TestFixtures.PROFILE_ID_4;
                Set<UUID> followers = new HashSet<>();
                followers.add(followerId1);
                followers.add(followerId2);

                when(followService.getFollowers(targetProfileId))
                                .thenReturn(followers);

                mockMvc.perform(get("/follows/{profileId}/followers", targetProfileId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)));
        }

        // ========== getFollowing Tests ==========

        @Test
        void getFollowing_ReturnsFollowingProfileIds() throws Exception {
                UUID followingId1 = TestFixtures.PROFILE_ID_2;
                UUID followingId2 = TestFixtures.PROFILE_ID_3;
                Set<UUID> following = new HashSet<>();
                following.add(followingId1);
                following.add(followingId2);

                when(followService.getFollowing(currentProfileId))
                                .thenReturn(following);

                mockMvc.perform(get("/follows/{profileId}/following", currentProfileId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)));
        }

        // ========== deleteUserData Tests ==========

        @Test
        void deleteUserData_WithValidUserId_ReturnsOkWithDeletionSummary() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 12;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(followService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "FOLLOW", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/follows/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)))
                                .andExpect(jsonPath("$.auditLogId").exists())
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.message").exists());

                verify(followService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithFollowsAndFollowers_ReturnsTotalCount() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 25; // 15 following + 10 followers
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(followService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "FOLLOW", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/follows/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)))
                                .andExpect(jsonPath("$.message", containsString("25")));

                verify(followService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithNoFollows_ReturnsZero() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(followService.deleteAllUserData(userId)).thenReturn(0);
                when(auditLogService.logDeletion(userId, "FOLLOW", 0)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/follows/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(0)));

                verify(followService, times(1)).deleteAllUserData(userId);
        }
}
