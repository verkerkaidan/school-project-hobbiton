package com.profileconnectionservice.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profileconnectionservice.TestFixtures;
import com.profileconnectionservice.config.TestSecurityConfiguration;
import com.profileconnectionservice.controller.dto.CreateReportRequest;
import com.profileconnectionservice.model.AuditLog;
import com.profileconnectionservice.model.Report;
import com.profileconnectionservice.service.AuditLogService;
import com.profileconnectionservice.service.ReportService;

@WebMvcTest(ReportController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class ReportControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ReportService reportService;

        @MockBean
        private AuditLogService auditLogService;

        @Autowired
        private ObjectMapper objectMapper;

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
        void createReport_WithValidRequest_ReturnsCreatedReportWith201() throws Exception {
                // Arrange
                CreateReportRequest request = new CreateReportRequest();
                request.setInitiatorId(initiatorId);
                request.setTargetId(targetId);
                request.setReason(Report.ReportReason.HARASSMENT);
                request.setDescription("User is harassing other users");

                Report savedReport = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.HARASSMENT,
                                "User is harassing other users");

                when(reportService.createReport(initiatorId, targetId, Report.ReportReason.HARASSMENT,
                                "User is harassing other users"))
                                .thenReturn(savedReport);

                // Act & Assert
                mockMvc.perform(post("/reports")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.initiator", is(initiatorId.toString())))
                                .andExpect(jsonPath("$.target", is(targetId.toString())))
                                .andExpect(jsonPath("$.reason", is("HARASSMENT")))
                                .andExpect(jsonPath("$.status", is("PENDING")));
        }

        @Test
        void createReport_WithSameInitiatorAndTarget_ReturnsBadRequest() throws Exception {
                // Arrange
                CreateReportRequest request = new CreateReportRequest();
                request.setInitiatorId(initiatorId);
                request.setTargetId(initiatorId); // Same as initiator
                request.setReason(Report.ReportReason.SPAM);
                request.setDescription("Self report");

                when(reportService.createReport(initiatorId, initiatorId, Report.ReportReason.SPAM, "Self report"))
                                .thenThrow(new IllegalArgumentException("Cannot report yourself"));

                // Act & Assert
                mockMvc.perform(post("/reports")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ========== getReport Tests ==========

        @Test
        void getReport_WithValidReportId_ReturnsReport() throws Exception {
                // Arrange
                Report report = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.SPAM,
                                "Spam report");

                when(reportService.getReportById(reportId))
                                .thenReturn(Optional.of(report));

                // Act & Assert
                mockMvc.perform(get("/reports/{reportId}", reportId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.initiator", is(initiatorId.toString())))
                                .andExpect(jsonPath("$.target", is(targetId.toString())))
                                .andExpect(jsonPath("$.reason", is("SPAM")));
        }

        @Test
        void getReport_WithNonExistentReportId_ReturnsNotFound() throws Exception {
                when(reportService.getReportById(reportId))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/reports/{reportId}", reportId))
                                .andExpect(status().isNotFound());
        }

        // ========== getReportsByTarget Tests ==========

        @Test
        void getReportsByTarget_ReturnsListOfReports() throws Exception {
                Report report1 = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.SPAM, "Report 1");
                Report report2 = TestFixtures.createReport(TestFixtures.PROFILE_ID_3, targetId,
                                Report.ReportReason.HARASSMENT, "Report 2");

                when(reportService.getReportsByTarget(targetId))
                                .thenReturn(List.of(report1, report2));

                mockMvc.perform(get("/reports/target/{targetId}", targetId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)))
                                .andExpect(jsonPath("$[0].target", is(targetId.toString())))
                                .andExpect(jsonPath("$[1].target", is(targetId.toString())));
        }

        // ========== getReportsByInitiator Tests ==========

        @Test
        void getReportsByInitiator_ReturnsListOfReports() throws Exception {
                Report report1 = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.SPAM, "Report 1");
                Report report2 = TestFixtures.createReport(initiatorId, TestFixtures.PROFILE_ID_3,
                                Report.ReportReason.HARASSMENT, "Report 2");

                when(reportService.getReportsByInitiator(initiatorId))
                                .thenReturn(List.of(report1, report2));

                mockMvc.perform(get("/reports/initiator/{initiatorId}", initiatorId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)))
                                .andExpect(jsonPath("$[0].initiator", is(initiatorId.toString())))
                                .andExpect(jsonPath("$[1].initiator", is(initiatorId.toString())));
        }

        // ========== getReportsByStatus Tests ==========

        @Test
        void getReportsByStatus_WithPendingStatus_ReturnsListOfPendingReports() throws Exception {
                Report report1 = TestFixtures.createPendingReport(initiatorId, targetId);
                Report report2 = TestFixtures.createPendingReport(TestFixtures.PROFILE_ID_3, TestFixtures.PROFILE_ID_4);

                when(reportService.getReportsByStatus(Report.ReportStatus.PENDING))
                                .thenReturn(List.of(report1, report2));

                mockMvc.perform(get("/reports/status/{status}", "PENDING"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)))
                                .andExpect(jsonPath("$[0].status", is("PENDING")))
                                .andExpect(jsonPath("$[1].status", is("PENDING")));
        }

        // ========== getPendingReports Tests ==========

        @Test
        void getPendingReports_ReturnsPendingReports() throws Exception {
                Report report1 = TestFixtures.createPendingReport(initiatorId, targetId);
                Report report2 = TestFixtures.createPendingReport(TestFixtures.PROFILE_ID_3, TestFixtures.PROFILE_ID_4);

                when(reportService.getPendingReports())
                                .thenReturn(List.of(report1, report2));

                mockMvc.perform(get("/reports/pending"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(2)))
                                .andExpect(jsonPath("$[0].status", is("PENDING")))
                                .andExpect(jsonPath("$[1].status", is("PENDING")));
        }

        // ========== updateReportStatus Tests ==========

        @Test
        void updateReportStatus_WithValidData_UpdatesAndReturnsReport() throws Exception {
                // Arrange
                Report updatedReport = TestFixtures.createReport(initiatorId, targetId, Report.ReportReason.HARASSMENT,
                                "Test");
                updatedReport.setStatus(Report.ReportStatus.UNDER_REVIEW);

                when(reportService.updateReportStatus(reportId, Report.ReportStatus.UNDER_REVIEW))
                                .thenReturn(updatedReport);

                String requestBody = "{\"status\": \"UNDER_REVIEW\"}";

                // Act & Assert
                mockMvc.perform(put("/reports/{reportId}/status", reportId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")));
        }

        @Test
        void updateReportStatus_WithNonExistentReportId_ReturnsNotFound() throws Exception {
                // Arrange
                when(reportService.updateReportStatus(reportId, Report.ReportStatus.RESOLVED))
                                .thenThrow(new IllegalArgumentException("Report not found"));

                String requestBody = "{\"status\": \"RESOLVED\"}";

                // Act & Assert
                mockMvc.perform(put("/reports/{reportId}/status", reportId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isNotFound());
        }

        // ========== deleteReport Tests ==========

        @Test
        void deleteReport_WithValidReportId_ReturnsNoContent() throws Exception {
                mockMvc.perform(delete("/reports/{reportId}", reportId))
                                .andExpect(status().isNoContent());
                verify(reportService, times(1)).deleteReport(reportId);
        }

        // ========== deleteUserData Tests ==========

        @Test
        void deleteUserData_WithValidUserId_ReturnsOkWithDeletionSummary() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 9;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(reportService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "REPORT", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/reports/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)))
                                .andExpect(jsonPath("$.auditLogId").exists())
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.message").exists());

                verify(reportService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithReportedByAndAbout_ReturnsTotalCount() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 18; // 10 reported by + 8 reported about
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(reportService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "REPORT", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/reports/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)))
                                .andExpect(jsonPath("$.message", containsString("18")));

                verify(reportService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithNoReports_ReturnsZero() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(reportService.deleteAllUserData(userId)).thenReturn(0);
                when(auditLogService.logDeletion(userId, "REPORT", 0)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/reports/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(0)));

                verify(reportService, times(1)).deleteAllUserData(userId);
        }

        @Test
        void deleteUserData_WithHighReportCount_ReturnsLargeCount() throws Exception {
                // Arrange
                UUID userId = TestFixtures.PROFILE_ID_1;
                int deletedCount = 150;
                AuditLog auditLog = new AuditLog();
                auditLog.setId(TestFixtures.AUDIT_LOG_ID_1);
                when(reportService.deleteAllUserData(userId)).thenReturn(deletedCount);
                when(auditLogService.logDeletion(userId, "REPORT", deletedCount)).thenReturn(auditLog);

                // Act & Assert
                mockMvc.perform(delete("/reports/user/{userUUID}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedCount", is(deletedCount)));

                verify(reportService, times(1)).deleteAllUserData(userId);
        }
}
