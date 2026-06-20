package com.profileservice.profileservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profileservice.profileservice.dao.ProfileDAO;
import com.profileservice.profileservice.dao.ProfileRepository;
import com.profileservice.profileservice.dto.ProfileDTO;
import com.profileservice.profileservice.models.Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProfileRepository profileRepository;

    private ProfileDAO profileDAO;
    private Profile createProfileResult;
    private Profile updateProfileResult;
    private UUID capturedCreateUserId;
    private ProfileDTO capturedCreateDto;
    private UUID capturedUpdateUserId;
    private ProfileDTO capturedUpdateDto;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createProfileResult = null;
        updateProfileResult = null;
        capturedCreateUserId = null;
        capturedCreateDto = null;
        capturedUpdateUserId = null;
        capturedUpdateDto = null;

        profileDAO = new ProfileDAO(null) {
            @Override
            public Profile createProfile(UUID userId, ProfileDTO dto) {
                capturedCreateUserId = userId;
                capturedCreateDto = dto;
                return createProfileResult;
            }

            @Override
            public Profile updateProfile(UUID userId, ProfileDTO dto) {
                capturedUpdateUserId = userId;
                capturedUpdateDto = dto;
                return updateProfileResult;
            }
        };

        ProfileController controller = new ProfileController(profileRepository, profileDAO);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId.toString(), null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProfile_ShouldReturnProfile() throws Exception {
        ProfileDTO dto = new ProfileDTO();
        dto.username = "testuser";
        dto.name = "Test User";

        Profile profile = new Profile(userId, "testuser", "Test User", null, null, null, null);
        createProfileResult = profile;

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));

        assertEquals(userId, capturedCreateUserId);
        assertNotNull(capturedCreateDto);
    }

    @Test
    void createProfile_ShouldReturnBadRequest_WhenUserAlreadyExists() throws Exception {
        ProfileDTO dto = new ProfileDTO();
        dto.username = "testuser";

        when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile()));

        mockMvc.perform(post("/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        assertNull(capturedCreateUserId);
    }

    @Test
    void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
        ProfileDTO dto = new ProfileDTO();
        dto.username = "updateduser";

        Profile existingProfile = new Profile(userId, "testuser", "Test User", null, null, null, null);
        Profile updatedProfile = new Profile(userId, "updateduser", "Test User", null, null, null, null);
        updateProfileResult = updatedProfile;

        when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));

        mockMvc.perform(put("/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));

        assertEquals(userId, capturedUpdateUserId);
        assertNotNull(capturedUpdateDto);
    }

    @Test
    void updateProfile_ShouldReturnBadRequest_WhenUserDoesNotExist() throws Exception {
        ProfileDTO dto = new ProfileDTO();

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        assertNull(capturedUpdateUserId);
    }

    @Test
    void deleteProfile_ShouldReturnSuccessMessage() throws Exception {
        Profile profile = new Profile();
        profile.setUserId(userId);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(delete("/profile/delete/{userId}", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile Succesfully deleted"));

        verify(profileRepository).delete(profile);
    }

    @Test
    void deleteProfile_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/profile/delete/{userId}", userId.toString()))
                .andExpect(status().isNotFound());

        verify(profileRepository, never()).delete(any());
    }

    @Test
    void getProfileByID_ShouldReturnProfile() throws Exception {
        Profile profile = new Profile(userId, "testuser", "Test User", "Bio", "pic.jpg", "link.com", "NYC");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/profile/{userId}", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.biography").value("Bio"))
                .andExpect(jsonPath("$.mediaPointer").value("pic.jpg"))
                .andExpect(jsonPath("$.link").value("link.com"))
                .andExpect(jsonPath("$.location").value("NYC"));
    }

    @Test
    void getProfileByID_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile/{userId}", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfileByUsername_ShouldReturnProfile() throws Exception {
        Profile profile = new Profile(userId, "testuser", "Test User", null, null, null, null);

        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/profile/by-username/{username}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getProfileByUsername_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        when(profileRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile/by-username/{username}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProfiles_ShouldReturnListOfSummaries() throws Exception {
        Profile profile1 = new Profile(UUID.randomUUID(), "user1", null, null, null, null, null);
        profile1.setMediaPointer("pic1.jpg");
        Profile profile2 = new Profile(UUID.randomUUID(), "user2", null, null, null, null, null);
        profile2.setMediaPointer("pic2.jpg");

        when(profileRepository.findAll()).thenReturn(List.of(profile1, profile2));

        mockMvc.perform(get("/profile/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].profilePictureUrl").value("pic1.jpg"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].profilePictureUrl").value("pic2.jpg"));
    }

    @Test
    void getAllProfiles_ShouldReturnEmptyList_WhenNoProfiles() throws Exception {
        when(profileRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/profile/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
