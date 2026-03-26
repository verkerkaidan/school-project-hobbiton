package com.profileconnectionservice.controller;

import static org.hamcrest.Matchers.is;
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
import com.profileconnectionservice.service.BlockService;

@WebMvcTest(BlockController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class BlockControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BlockService blockService;

        @MockBean
        private AuditLogService auditLogService;

        private UUID currentProfileId;
        private UUID targetProfileId;

        @BeforeEach
        void setUp() {
                currentProfileId = TestFixtures.PROFILE_ID_1;
                targetProfileId = TestFixtures.PROFILE_ID_2;
        }

        // ========== toggleBlock Tests ==========

        @Test
        void toggleBlock_WhenBlockDoesNotExist_ReturnsTrue() throws Exception {
                // Only test business logic via valid request
                when(blockService.toggleBlock(currentProfileId, targetProfileId))
                                .thenReturn(true);

                mockMvc.perform(put("/blocks/{targetProfileId}", targetProfileId)
                                .header("X-Profile-Id", currentProfileId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));
        }

        @Test
        void toggleBlock_WhenBlockExists_ReturnsFalse() throws Exception {
                when(blockService.toggleBlock(currentProfileId, targetProfileId))
                                .thenReturn(false);

                mockMvc.perform(put("/blocks/{targetProfileId}", targetProfileId)
                                .header("X-Profile-Id", currentProfileId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().string("false"));
        }

        // ========== getBlocked Tests ==========

        @Test
        void getBlocked_ReturnsBlockedProfileIds() throws Exception {
                UUID blockedId1 = TestFixtures.PROFILE_ID_2;
                UUID blockedId2 = TestFixtures.PROFILE_ID_3;
                Set<UUID> blocked = new HashSet<>();
                blocked.add(blockedId1);
                blocked.add(blockedId2);

                when(blockService.getBlocked(currentProfileId))
                                .thenReturn(blocked);

                mockMvc.perform(get("/blocks/{profileId}/blocked", currentProfileId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)));
        }

        // ========== deleteUserData Tests ==========

        @Test
        void deleteUserData_WithValidUserId_ReturnsOkWithDeletionSummary() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 5;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(blockService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "BLOCK", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/blocks/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)))
                                .andExpect(jsonPath("$.auditLogId").exists())
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.message").exists());

                verify(blockService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithNoRecords_ReturnsZeroCount() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(blockService.deleteAllUserData(userId)).thenReturn(0);
                when(auditLogService.logDeletion(userId, "BLOCK", 0)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/blocks/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(0)))
                                .andExpect(jsonPath("$.auditLogId").exists());

                verify(blockService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithLargeNumberOfRecords_ReturnsCorrectCount() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 150;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(blockService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "BLOCK", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/blocks/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)));

                verify(blockService, times(1)).deleteAllUserData(userId);
        }
}
