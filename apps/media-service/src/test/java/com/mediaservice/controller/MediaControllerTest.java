package com.mediaservice.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mediaservice.dto.ContentFlagUpdateDTO;
import com.mediaservice.dto.MediaFileDTO;
import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.MediaFile;
import com.mediaservice.service.MediaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    @Test
    @DisplayName("Retrieving a list of media files from the same owner")
    void retrieveMediaByOwner_returnsCorrectDTOList() {
        // Arrange
        UUID ownerId = UUID.randomUUID();

        MediaFile file1 = new MediaFile(new byte[]{1,2,3}, new byte[]{1,2}, ContentFlag.UNASSIGNED, "image/png");
        MediaFile file2 = new MediaFile(new byte[]{3,4,5}, new byte[]{3, 4}, ContentFlag.UNASSIGNED, "image/png");

        when(mediaService.getMediaFilesByOwner(ownerId)).thenReturn(List.of(file1, file2));

        // Act
        ResponseEntity<List<MediaFileDTO>> response = mediaController.retrieveMediaByOwner(ownerId);

        // Assert status
        assertEquals(200, response.getStatusCodeValue());

        // Assert body
        List<MediaFileDTO> dtoList = response.getBody();
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());

        assertArrayEquals(file1.getThumbnail(), dtoList.get(0).fileThumb);
        assertArrayEquals(file1.getOriginal(), dtoList.get(0).fileData);
        assertEquals(file1.getContentType(), dtoList.get(0).contentType);
        assertEquals(file1.getContentFlag(), dtoList.get(0).contentFlag);

        assertArrayEquals(file2.getThumbnail(), dtoList.get(1).fileThumb);
        assertArrayEquals(file2.getOriginal(), dtoList.get(1).fileData);
        assertEquals(file2.getContentType(), dtoList.get(1).contentType);
        assertEquals(file2.getContentFlag(), dtoList.get(1).contentFlag);

        // Verify service interaction
        verify(mediaService, times(1)).getMediaFilesByOwner(ownerId);
        verifyNoMoreInteractions(mediaService);
    }

    @Test
    @DisplayName("Updating content flag should be done correctly")
    void updateContentFlag_ContentFlagUpdateDTOList_UUIDList() {
        // Arrange
        UUID fileId1 = UUID.randomUUID();
        UUID fileId2 = UUID.randomUUID();

        ContentFlagUpdateDTO dto1 = new ContentFlagUpdateDTO(ContentFlag.RISK, fileId1);
        ContentFlagUpdateDTO dto2 = new ContentFlagUpdateDTO(ContentFlag.SAFE, fileId2);

        List<ContentFlagUpdateDTO> input = List.of(dto1, dto2);

        // Act
        ResponseEntity<List<UUID>> response = mediaController.updateContentFlag(input);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of(fileId1, fileId2), response.getBody());

        verify(mediaService).updateMediaContentFlag(fileId1, ContentFlag.RISK);
        verify(mediaService).updateMediaContentFlag(fileId2, ContentFlag.SAFE);
        verifyNoMoreInteractions(mediaService);
    }

    @Test
    void getUnassignedMediaFiles_returnsCorrectDTOs() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        MediaFile file1 = new MediaFile();
        file1.setFileId(id1);
        file1.setContentFlag(ContentFlag.UNASSIGNED);
        MediaFile file2 = new MediaFile();
        file2.setFileId(id2);
        file2.setContentFlag(ContentFlag.UNASSIGNED);

        when(mediaService.getAllUnassigned()).thenReturn(List.of(file1, file2));

        // Act
        ResponseEntity<List<ContentFlagUpdateDTO>> response = mediaController.getUnassignedMediaFiles();

        // Assert status code
        assertEquals(200, response.getStatusCodeValue());

        // Assert body content
        List<ContentFlagUpdateDTO> dtos = response.getBody();
        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        assertEquals(id1, dtos.get(0).fileId);
        assertEquals(id2, dtos.get(1).fileId);

        // Verify service call
        verify(mediaService, times(1)).getAllUnassigned();
        verifyNoMoreInteractions(mediaService);
    }
}
