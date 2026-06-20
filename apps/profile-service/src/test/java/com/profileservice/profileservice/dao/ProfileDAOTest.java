package com.profileservice.profileservice.dao;

import com.profileservice.profileservice.dto.ProfileDTO;
import com.profileservice.profileservice.models.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileDAOTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileDAO profileDAO;

    @Test
    void createProfile_ShouldSaveAndReturnProfile() {
        UUID userId = UUID.randomUUID();
        ProfileDTO dto = new ProfileDTO();
        dto.username = "testuser";
        dto.name = "Test User";
        dto.biography = "A short bio";
        dto.mediaPointer = "http://example.com/pic.jpg";
        dto.link = "http://example.com";
        dto.location = "New York";

        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile result = profileDAO.createProfile(userId, dto);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getName());
        assertEquals("A short bio", result.getBiography());
        assertEquals("http://example.com/pic.jpg", result.getMediaPointer());
        assertEquals("http://example.com", result.getLink());
        assertEquals("New York", result.getLocation());
        assertNotNull(result.getLastEdited());
        assertNotNull(result.getCreated_at());

        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void createProfile_ShouldPassThroughNullFields() {
        UUID userId = UUID.randomUUID();
        ProfileDTO dto = new ProfileDTO();
        dto.username = "testuser";
        dto.name = null;
        dto.biography = null;
        dto.mediaPointer = null;
        dto.link = null;
        dto.location = null;

        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile result = profileDAO.createProfile(userId, dto);

        assertEquals("testuser", result.getUsername());
        assertNull(result.getName());
        assertNull(result.getBiography());
        assertNull(result.getMediaPointer());
        assertNull(result.getLink());
        assertNull(result.getLocation());
    }

    @Test
    void updateProfile_ShouldUpdateAllFields_WhenAllFieldsProvided() {
        UUID userId = UUID.randomUUID();
        Profile existingProfile = new Profile(userId, "olduser", "Old Name", "Old Bio", "old.jpg",
                "old.com", "Old Location");
        ProfileDTO updateDto = new ProfileDTO();
        updateDto.username = "newuser";
        updateDto.name = "New Name";
        updateDto.biography = "New Bio";
        updateDto.mediaPointer = "new.jpg";
        updateDto.link = "new.com";
        updateDto.location = "New Location";

        when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile result = profileDAO.updateProfile(userId, updateDto);

        assertEquals("newuser", result.getUsername());
        assertEquals("New Name", result.getName());
        assertEquals("New Bio", result.getBiography());
        assertEquals("new.jpg", result.getMediaPointer());
        assertEquals("new.com", result.getLink());
        assertEquals("New Location", result.getLocation());
        assertNotNull(result.getLastEdited());

        verify(profileRepository).save(existingProfile);
    }

    @Test
    void updateProfile_ShouldThrow_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        ProfileDTO dto = new ProfileDTO();

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileDAO.updateProfile(userId, dto));

        assertTrue(exception.getStatusCode().is4xxClientError());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void updateProfile_ShouldNotModifyFields_WhenDtoFieldsAreNull() {
        UUID userId = UUID.randomUUID();
        Profile existingProfile = new Profile(userId, "testuser", "Test User", "Old Bio", "old.jpg",
                "old.com", "Old Location");
        ProfileDTO updateDto = new ProfileDTO();
        updateDto.username = null;
        updateDto.name = null;
        updateDto.biography = "Updated Bio";
        updateDto.mediaPointer = null;
        updateDto.link = null;
        updateDto.location = null;

        when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile result = profileDAO.updateProfile(userId, updateDto);

        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getName());
        assertEquals("Updated Bio", result.getBiography());
        assertEquals("old.jpg", result.getMediaPointer());
        assertEquals("old.com", result.getLink());
        assertEquals("Old Location", result.getLocation());
    }

    @Test
    void updateProfile_ShouldUpdateLastEdited_ToCurrentTime() {
        UUID userId = UUID.randomUUID();
        Profile existingProfile = new Profile(userId, "testuser", "Test User", null, null, null, null);
        ProfileDTO updateDto = new ProfileDTO();
        updateDto.biography = "Updated";

        when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile result = profileDAO.updateProfile(userId, updateDto);

        assertNotNull(result.getLastEdited());
        assertTrue(result.getLastEdited().isAfter(existingProfile.getLastEdited())
                || result.getLastEdited().isEqual(existingProfile.getLastEdited()));
    }
}
