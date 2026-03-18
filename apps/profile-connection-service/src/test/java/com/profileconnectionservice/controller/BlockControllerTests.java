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
import com.profileconnectionservice.service.BlockService;

@WebMvcTest(BlockController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class BlockControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BlockService blockService;

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
}
