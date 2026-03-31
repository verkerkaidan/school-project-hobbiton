package com.mediaservice.service;

import com.mediaservice.dao.ReportDAO;
import com.mediaservice.dto.ContentFlagUpdateDTO;
import com.mediaservice.dto.ReportUpdatedDTO;
import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.FileReport;
import com.mediaservice.model.MediaFile;
import com.mediaservice.model.ReportedFile;
import com.mediaservice.repository.ReportedFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    @InjectMocks
    private ReportService reportService;

    @Mock
    private MediaService mediaService;
    @Mock
    private ReportedFileRepository reportedFileRepository;
    @Mock
    private ReportDAO reportDAO;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("Should create new report and return reportId")
    void createNewReport_validInput_returnsReportId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();

        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(fileId);

        when(mediaService.getMediaFile(instanceId)).thenReturn(mockMediaFile);

        // Simulate DB assigning reportId
        doAnswer(invocation -> {
            FileReport report = invocation.getArgument(0);
            report.setReportId(reportId);
            return null;
        }).when(reportDAO).saveReport(any(FileReport.class));

        // Act
        UUID result = reportService.createNewReport(userId, instanceId);

        // Assert
        ArgumentCaptor<FileReport> captor = ArgumentCaptor.forClass(FileReport.class);
        verify(reportDAO).saveReport(captor.capture());

        FileReport savedReport = captor.getValue();

        assertEquals(userId, savedReport.getUserID());
        assertEquals(fileId, savedReport.getFileId());

        assertNotNull(result);
        assertEquals(reportId, result);
    }

    @Test
    @DisplayName("Should publish updates and delete reports for each DTO")
    void updateReports_validInput_processesEachDto() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        ContentFlagUpdateDTO dto =
                new ContentFlagUpdateDTO(ContentFlag.RISK, fileId, "Test note");

        List<ContentFlagUpdateDTO> dtoList = List.of(dto);

        // Mock reports
        FileReport report1 = new FileReport();
        report1.setUserID(user1);

        FileReport report2 = new FileReport();
        report2.setUserID(user2);

        when(reportDAO.getFileReportsByFileId(fileId))
                .thenReturn(List.of(report1, report2));

        // Spy service to verify internal method call
        ReportService spyService = Mockito.spy(reportService);

        doNothing().when(spyService).publishReportUpdated(any(ReportUpdatedDTO.class));

        // Act
        spyService.updateReports(dtoList);

        // Assert DAO calls
        verify(reportDAO).getFileReportsByFileId(fileId);
        verify(reportDAO).deleteAllReportsByFileId(fileId);

        // Capture published event
        ArgumentCaptor<ReportUpdatedDTO> captor =
                ArgumentCaptor.forClass(ReportUpdatedDTO.class);

        verify(spyService).publishReportUpdated(captor.capture());

        ReportUpdatedDTO event = captor.getValue();

        assertNotNull(event);
        assertEquals("Test note", event.note);
        assertEquals(2, event.userIds.size());
        assertTrue(event.userIds.contains(user1));
        assertTrue(event.userIds.contains(user2));
    }

    @Test
    @DisplayName("Should publish report updated event to RabbitMQ")
    void publishReportUpdated_validEvent_sendsMessage() {
        // Arrange
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        String note = "Test note";

        ReportUpdatedDTO event = new ReportUpdatedDTO(userIds, note);

        // Act
        reportService.publishReportUpdated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                "HobbyApp_reportEvents",
                "report.updated",
                event
        );
    }

    @Test
    @DisplayName("Should create new banned file and save it to repository")
    void createNewBannedFile_validFileId_savesReportedFile() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        byte[] mockHash = "mockHash".getBytes();

        MediaFile mockMediaFile = new MediaFile();
        mockMediaFile.setFileId(fileId);
        mockMediaFile.setHash(mockHash);

        when(mediaService.getMediaFileByFileId(fileId)).thenReturn(mockMediaFile);

        // Act
        reportService.createNewBannedFile(fileId);

        // Assert
        ArgumentCaptor<ReportedFile> captor = ArgumentCaptor.forClass(ReportedFile.class);
        verify(reportedFileRepository).save(captor.capture());

        ReportedFile saved = captor.getValue();
        assertArrayEquals(mockHash, saved.getHash());

        // Verify interaction with mediaService
        verify(mediaService).getMediaFileByFileId(fileId);
    }

    @Test
    @DisplayName("Should return all distinct media files based on report DAO")
    void getAllDistinctReports_returnsMediaFiles() {
        // Arrange
        UUID fileId1 = UUID.randomUUID();
        UUID fileId2 = UUID.randomUUID();

        MediaFile media1 = new MediaFile();
        media1.setFileId(fileId1);

        MediaFile media2 = new MediaFile();
        media2.setFileId(fileId2);

        // Mock DAO to return file IDs
        when(reportDAO.getAllDistinctFiles()).thenReturn(List.of(fileId1, fileId2));

        // Mock service to return MediaFile objects
        when(mediaService.getMediaFileByFileId(fileId1)).thenReturn(media1);
        when(mediaService.getMediaFileByFileId(fileId2)).thenReturn(media2);

        // Act
        List<MediaFile> result = reportService.getAllDistinctReports();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(media1));
        assertTrue(result.contains(media2));

        // Verify interactions
        verify(reportDAO).getAllDistinctFiles();
        verify(mediaService).getMediaFileByFileId(fileId1);
        verify(mediaService).getMediaFileByFileId(fileId2);
    }
}
