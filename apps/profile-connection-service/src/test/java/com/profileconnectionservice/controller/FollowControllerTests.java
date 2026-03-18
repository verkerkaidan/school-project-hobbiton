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
import com.profileconnectionservice.service.FollowService;

@WebMvcTest(FollowController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class FollowControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private FollowService followService;

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
}
