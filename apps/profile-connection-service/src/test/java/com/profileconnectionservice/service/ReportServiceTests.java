package com.profileconnectionservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.profileconnectionservice.TestFixtures;
import com.profileconnectionservice.model.Report;
import com.profileconnectionservice.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTests {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReportService reportService;

    private UUID initiatorId;
    private UUID targetId;
    private UUID reportId;

    @BeforeEach
    void setUp() {
        initiatorId = TestFixtures.PROFILE_ID_1;
        targetId = TestFixtures.PROFILE_ID_2;
        reportId = TestFixtures.REPORT_ID_1;
    }

    // ========== createReport Tests ==========

    @Test
    void createReport_WithValidData_SavesAndReturnsReport() {
        // Arrange
        Report.ReportReason reason = Report.ReportReason.HARASSMENT;
        String description = "Inappropriate behavior";
        Report savedReport = TestFixtures.createReport(initiatorId, targetId, reason, description);

        when(reportRepository.save(any(Report.class)))
                .thenReturn(savedReport);

        // Act
        Report result = reportService.createReport(initiatorId, targetId, reason, description);

        // Assert
        assertNotNull(result);
        assertEquals(initiatorId, result.getInitiator());
        assertEquals(targetId, result.getTarget());
        assertEquals(reason, result.getReason());
        assertEquals(description, result.getDescription());
        assertEquals(Report.ReportStatus.PENDING, result.getStatus());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void createReport_WithSameInitiatorAndTarget_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(initiatorId, initiatorId, Report.ReportReason.SPAM, "Test");
        });
        verify(reportRepository, never()).save(any());
    }

    // ========== getReportById Tests ==========

    @Test
    void getReportById_WhenReportExists_ReturnsReport() {
        // Arrange
        Report report = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.SPAM, "Test");
        when(reportRepository.findById(reportId))
                .thenReturn(Optional.of(report));

        // Act
        Optional<Report> result = reportService.getReportById(reportId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(report, result.get());
    }

    // ========== getReportsByTarget Tests ==========

    @Test
    void getReportsByTarget_ReturnsListOfReports() {
        // Arrange
        Report report1 = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.SPAM, "Report 1");
        Report report2 = TestFixtures.createReport(TestFixtures.PROFILE_ID_3, targetId, Report.ReportReason.HARASSMENT,
                "Report 2");

        when(reportRepository.findByTarget(targetId))
                .thenReturn(List.of(report1, report2));

        // Act
        List<Report> result = reportService.getReportsByTarget(targetId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(report1));
        assertTrue(result.contains(report2));
    }

    // ========== getReportsByInitiator Tests ==========

    @Test
    void getReportsByInitiator_ReturnsListOfReports() {
        // Arrange
        Report report1 = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.SPAM, "Report 1");
        Report report2 = TestFixtures.createReport(initiatorId, TestFixtures.PROFILE_ID_3,
                Report.ReportReason.HARASSMENT, "Report 2");

        when(reportRepository.findByInitiator(initiatorId))
                .thenReturn(List.of(report1, report2));

        // Act
        List<Report> result = reportService.getReportsByInitiator(initiatorId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(report1));
        assertTrue(result.contains(report2));
    }

    // ========== getReportsByStatus Tests ==========

    @Test
    void getReportsByStatus_ReturnsListOfReportsWithStatus() {
        // Arrange
        Report report1 = TestFixtures.createPendingReport(initiatorId, targetId);
        Report report2 = TestFixtures.createPendingReport(TestFixtures.PROFILE_ID_3, TestFixtures.PROFILE_ID_4);

        when(reportRepository.findByStatus(Report.ReportStatus.PENDING))
                .thenReturn(List.of(report1, report2));

        // Act
        List<Report> result = reportService.getReportsByStatus(Report.ReportStatus.PENDING);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStatus() == Report.ReportStatus.PENDING));
    }

    // ========== updateReportStatus Tests ==========

    @Test
    void updateReportStatus_WithValidReportId_UpdatesStatusAndReturnsReport() {
        // Arrange
        Report report = TestFixtures.createPendingReport(initiatorId, targetId);
        Report updatedReport = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.HARASSMENT, "Test");
        updatedReport.setStatus(Report.ReportStatus.UNDER_REVIEW);

        when(reportRepository.findById(reportId))
                .thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class)))
                .thenReturn(updatedReport);

        // Act
        Report result = reportService.updateReportStatus(reportId, Report.ReportStatus.UNDER_REVIEW);

        // Assert
        assertNotNull(result);
        assertEquals(Report.ReportStatus.UNDER_REVIEW, result.getStatus());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void updateReportStatus_WithInvalidReportId_ThrowsIllegalArgumentException() {
        // Arrange
        when(reportRepository.findById(reportId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reportService.updateReportStatus(reportId, Report.ReportStatus.RESOLVED);
        });
        verify(reportRepository, never()).save(any());
    }

    // ========== getPendingReports Tests ==========

    @Test
    void getPendingReports_ReturnsListOfPendingReports() {
        // Arrange
        Report report1 = TestFixtures.createPendingReport(initiatorId, targetId);
        Report report2 = TestFixtures.createPendingReport(TestFixtures.PROFILE_ID_3, TestFixtures.PROFILE_ID_4);

        when(reportRepository.findByStatus(Report.ReportStatus.PENDING))
                .thenReturn(List.of(report1, report2));

        // Act
        List<Report> result = reportService.getPendingReports();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStatus() == Report.ReportStatus.PENDING));
    }

    // ========== deleteReport Tests ==========

    @Test
    void deleteReport_CallsRepositoryDelete() {
        // Act
        reportService.deleteReport(reportId);

        // Assert
        verify(reportRepository, times(1)).deleteById(reportId);
    }

    // ========== deleteAllUserData Tests ==========

    @Test
    void deleteAllUserData_WithValidUser_DeletesReportsAndReturnsCount() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        int deletedAsInitiator = 5;
        int deletedAsTarget = 3;
        when(reportRepository.deleteByInitiator(userId)).thenReturn(deletedAsInitiator);
        when(reportRepository.deleteByTarget(userId)).thenReturn(deletedAsTarget);

        // Act
        int result = reportService.deleteAllUserData(userId);

        // Assert
        assertEquals(8, result);
        verify(reportRepository, times(1)).deleteByInitiator(userId);
        verify(reportRepository, times(1)).deleteByTarget(userId);
    }

    @Test
    void deleteAllUserData_WithReportedByOnly_ReturnsOnlyInitiatorCount() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        when(reportRepository.deleteByInitiator(userId)).thenReturn(4);
        when(reportRepository.deleteByTarget(userId)).thenReturn(0);

        // Act
        int result = reportService.deleteAllUserData(userId);

        // Assert
        assertEquals(4, result);
        verify(reportRepository, times(1)).deleteByInitiator(userId);
        verify(reportRepository, times(1)).deleteByTarget(userId);
    }

    @Test
    void deleteAllUserData_WithReportedAboutOnly_ReturnsOnlyTargetCount() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        when(reportRepository.deleteByInitiator(userId)).thenReturn(0);
        when(reportRepository.deleteByTarget(userId)).thenReturn(6);

        // Act
        int result = reportService.deleteAllUserData(userId);

        // Assert
        assertEquals(6, result);
        verify(reportRepository, times(1)).deleteByInitiator(userId);
        verify(reportRepository, times(1)).deleteByTarget(userId);
    }

    @Test
    void deleteAllUserData_WithZeroReports_ReturnsZero() {
        // Arrange
        UUID userId = TestFixtures.PROFILE_ID_1;
        when(reportRepository.deleteByInitiator(userId)).thenReturn(0);
        when(reportRepository.deleteByTarget(userId)).thenReturn(0);

        // Act
        int result = reportService.deleteAllUserData(userId);

        // Assert
        assertEquals(0, result);
        verify(reportRepository, times(1)).deleteByInitiator(userId);
        verify(reportRepository, times(1)).deleteByTarget(userId);
    }
}
