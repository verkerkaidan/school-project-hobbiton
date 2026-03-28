package com.mediaservice.service;

import com.mediaservice.dao.MediaFileDAO;
import com.mediaservice.dao.MediaInstanceDAO;
import com.mediaservice.dto.MediaEntryDTO;
import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import com.mediaservice.repository.ReportedFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {
    @InjectMocks
    private MediaService mediaService;

    @Mock
    private CompressionService compressionService;
    @Mock
    private ReportService reportService;

    @Mock
    private MediaFileDAO mediaFileDAO;

    @Mock
    private MediaInstanceDAO mediaInstanceDAO;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private ReportedFileRepository reportedFileRepository;

    @Test
    @DisplayName("Should upload new media file and mediainstance when file is not yet known in database")
    public void uploadNewFile_MediaEntryDTOAndUUID_returnsUUID() {
        // Variable setup
        String fileUri = "fileUri";
        byte[] mockHash = fileUri.getBytes();
        UUID ownerId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        // Object setup
        MediaEntryDTO mediaEntryDTO = new MediaEntryDTO();
        mediaEntryDTO.fileUri = fileUri;
        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(fileId);

        // Mocking
        when(encryptionService.createHash(fileUri)).thenReturn(mockHash);
        when(mediaFileDAO.doesMediaFileExist(mockHash)).thenReturn(false);

        // Spy the service to override internal behavior
        MediaService spyService = Mockito.spy(mediaService);

        // Override private logic indirectly via spy
        doReturn(mockMediaFile)
                .when(spyService)
                .createMediaFile(mediaEntryDTO, mockHash);

        doNothing()
                .when(spyService)
                .addFileThumbnail(mockMediaFile);

        doAnswer(invocation -> {
            MediaInstance instance = invocation.getArgument(0);
            instance.setInstanceId(UUID.randomUUID()); // simulate DB
            return null;
        }).when(mediaInstanceDAO).saveMediaInstance(any(MediaInstance.class));

        // Act
        UUID result = spyService.uploadMediaFile(mediaEntryDTO, ownerId);

        // Assert
        verify(mediaInstanceDAO).saveMediaInstance(any(MediaInstance.class));
        verify(mediaFileDAO).saveMediaFile(mockMediaFile);
        verify(mediaFileDAO, never()).getMediaFile(any(byte[].class));

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should not save mediaFile, but should create new mediaInstance for file known in database")
    public void uploadExistingFile_MediaEntryDTOAndUUID_returnsUUID() {
        // variables
        String fileUri = "fileUri";
        byte[] mockHash = fileUri.getBytes();
        UUID ownerId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        // objects
        MediaEntryDTO mediaEntryDTO = new MediaEntryDTO();
        mediaEntryDTO.fileUri = fileUri;
        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(fileId);

        // Mocking
        when(encryptionService.createHash(fileUri)).thenReturn(mockHash);
        when(mediaFileDAO.doesMediaFileExist(mockHash)).thenReturn(true);
        when(mediaFileDAO.getMediaFile(mockHash)).thenReturn(mockMediaFile);

        doAnswer(invocation -> {
            MediaInstance instance = invocation.getArgument(0);
            instance.setInstanceId(UUID.randomUUID()); // simulate DB
            return null;
        }).when(mediaInstanceDAO).saveMediaInstance(any(MediaInstance.class));

        // Act
        UUID result = mediaService.uploadMediaFile(mediaEntryDTO, ownerId);

        // Assert
        verify(mediaInstanceDAO).saveMediaInstance(any(MediaInstance.class));
        verify(mediaFileDAO, never()).saveMediaFile(mockMediaFile);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should retrieve a single mediaFile, from a given instanceId")
    public void getMediaFile_UUID_returnsMediaFile() {
        // Variables
        UUID mockInstanceId = UUID.randomUUID();
        UUID mockFileId = UUID.randomUUID();
        UUID mockOwnerId = UUID.randomUUID();

        // Objects
        MediaInstance mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        mockMediaInstance.setInstanceId(mockInstanceId);
        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(mockFileId);

        // Mocking
        when(mediaInstanceDAO.getMediaInstance(mockInstanceId)).thenReturn(mockMediaInstance);
        when(mediaFileDAO.getMediaFile(mockMediaInstance.getFileId())).thenReturn(mockMediaFile);

        // Act
        MediaFile result = mediaService.getMediaFile(mockInstanceId);

        // Assert
        verify(mediaInstanceDAO).getMediaInstance(any(UUID.class));
        verify(mediaFileDAO).getMediaFile(any(UUID.class));

        assertEquals(result.getFileId(), mockFileId);
    }

    @Test
    @DisplayName("Should retrieve multiple mediaFiles, from a given ownerId")
    public void getMediaFilesByOwner_UUID_returnsListMediaFile() {
        // Variables
        UUID first_mockInstanceId = UUID.randomUUID();
        UUID second_mockInstanceId = UUID.randomUUID();
        UUID third_mockInstanceId = UUID.randomUUID();
        UUID mockFileId = UUID.randomUUID();
        UUID mockOwnerId = UUID.randomUUID();

        // Objects
        MediaInstance first_mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        first_mockMediaInstance.setInstanceId(first_mockInstanceId);
        MediaInstance second_mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        second_mockMediaInstance.setInstanceId(second_mockInstanceId);
        MediaInstance third_mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        third_mockMediaInstance.setInstanceId(third_mockInstanceId);
        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(mockFileId);

        List<MediaInstance> mockMediaInstanceList = new ArrayList<>();
        mockMediaInstanceList.add(first_mockMediaInstance);
        mockMediaInstanceList.add(second_mockMediaInstance);
        mockMediaInstanceList.add(third_mockMediaInstance);
        List<MediaFile> mockMediaFileList = new ArrayList<>();
        mockMediaFileList.add(mockMediaFile);
        mockMediaFileList.add(mockMediaFile);
        mockMediaFileList.add(mockMediaFile);

        // Mocking
        when(mediaInstanceDAO.getAllMediaInstancesByOwnerId(mockOwnerId)).thenReturn(mockMediaInstanceList);
        when(mediaFileDAO.getCollectionOfMediaFiles(any())).thenReturn(mockMediaFileList);

        List<MediaFile> result = mediaService.getMediaFilesByOwner(mockOwnerId);

        verify(mediaInstanceDAO, atMostOnce()).getAllMediaInstancesByOwnerId(any(UUID.class));
        verify(mediaFileDAO, atMostOnce()).getCollectionOfMediaFiles(any());

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should not try to delete a mediaFile if one or more referring instances are remaining")
    public void deleteMediaInstance_UUID_void() {
        // Variables
        UUID first_mockInstanceId = UUID.randomUUID();
        UUID second_mockInstanceId = UUID.randomUUID();
        UUID mockFileId = UUID.randomUUID();
        UUID mockOwnerId = UUID.randomUUID();

        // Objects
        MediaInstance first_mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        first_mockMediaInstance.setInstanceId(first_mockInstanceId);
        MediaInstance second_mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        second_mockMediaInstance.setInstanceId(second_mockInstanceId);
        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(mockFileId);

        List<MediaInstance> mockMediaInstanceList = new ArrayList<>();
        mockMediaInstanceList.add(second_mockMediaInstance);

        // Mocking
        when(mediaInstanceDAO.getMediaInstance(first_mockInstanceId)).thenReturn(first_mockMediaInstance);
        when(mediaInstanceDAO.getAllMediaInstancesByMediaFileId(any())).thenReturn(mockMediaInstanceList);
        doNothing()
                .when(mediaInstanceDAO)
                .deleteMediaInstance(any());

        // Act
        mediaService.deleteMediaInstance(first_mockInstanceId);

        // Assert
        verify(mediaFileDAO, never()).getMediaFile(any(UUID.class));
    }

    @Test
    @DisplayName("Should delete a mediaFile if no more referring instances are remaining")
    public void deleteMediaInstance_AndFile_UUID_void() {
        // Variables
        UUID mockInstanceId = UUID.randomUUID();
        UUID mockFileId = UUID.randomUUID();
        UUID mockOwnerId = UUID.randomUUID();

        // Objects
        MediaInstance mockMediaInstance = new MediaInstance(mockFileId, mockOwnerId);
        mockMediaInstance.setInstanceId(mockInstanceId);
        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(mockFileId);

        List<MediaInstance> mockMediaInstanceList = new ArrayList<>();

        // Mocking
        when(mediaInstanceDAO.getMediaInstance(mockInstanceId)).thenReturn(mockMediaInstance);
        when(mediaInstanceDAO.getAllMediaInstancesByMediaFileId(any())).thenReturn(mockMediaInstanceList);
        when(mediaFileDAO.getMediaFile(any(UUID.class))).thenReturn(mockMediaFile);
        doNothing()
                .when(mediaInstanceDAO)
                .deleteMediaInstance(any());
        doNothing()
                .when(mediaFileDAO)
                .deleteMediaFile(any(MediaFile.class));

        // Act
        mediaService.deleteMediaInstance(mockInstanceId);

        // Assert
        verify(mediaFileDAO).getMediaFile(any(UUID.class));
        verify(mediaFileDAO).deleteMediaFile(any(MediaFile.class));
    }
}
