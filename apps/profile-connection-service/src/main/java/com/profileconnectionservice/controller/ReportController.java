package com.profileconnectionservice.controller;

import com.profileconnectionservice.controller.dto.CreateReportRequest;
import com.profileconnectionservice.model.AuditLog;
import com.profileconnectionservice.model.Report;
import com.profileconnectionservice.service.AuditLogService;
import com.profileconnectionservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody CreateReportRequest request) {
        log.debug("Received POST /reports with initiator={}, target={}", request.getInitiatorId(),
                request.getTargetId());
        try {
            Report report = reportService.createReport(request.getInitiatorId(), request.getTargetId(),
                    request.getReason(), request.getDescription());
            log.debug("Report created successfully: {}", report.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid report creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error during createReport: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<Report> getReport(@PathVariable UUID reportId) {
        log.debug("Received GET /reports/{}", reportId);
        Optional<Report> report = reportService.getReportById(reportId);
        if (report.isPresent()) {
            log.debug("Report found: {}", reportId);
            return ResponseEntity.ok(report.get());
        } else {
            log.debug("Report not found: {}", reportId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<Report>> getReportsByTarget(@PathVariable UUID targetId) {
        log.debug("Received GET /reports/target/{}", targetId);
        List<Report> reports = reportService.getReportsByTarget(targetId);
        log.debug("Found {} reports for target {}", reports.size(), targetId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/initiator/{initiatorId}")
    public ResponseEntity<List<Report>> getReportsByInitiator(@PathVariable UUID initiatorId) {
        log.debug("Received GET /reports/initiator/{}", initiatorId);
        List<Report> reports = reportService.getReportsByInitiator(initiatorId);
        log.debug("Found {} reports by initiator {}", reports.size(), initiatorId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Report>> getAllReports() {
        log.debug("Received GET /reports/all");
        List<Report> reports = reportService.getAllReports();
        log.debug("Found {} total reports", reports.size());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Report>> getReportsByStatus(@PathVariable Report.ReportStatus status) {
        log.debug("Received GET /reports/status/{}", status);
        List<Report> reports = reportService.getReportsByStatus(status);
        log.debug("Found {} reports with status {}", reports.size(), status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports() {
        log.debug("Received GET /reports/pending");
        List<Report> reports = reportService.getPendingReports();
        log.debug("Found {} pending reports", reports.size());
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{reportId}/status")
    public ResponseEntity<Report> updateReportStatus(
            @PathVariable UUID reportId,
            @RequestBody Map<String, Report.ReportStatus> request) {
        log.debug("Received PUT /reports/{}/status with newStatus={}", reportId, request.get("status"));
        try {
            Report.ReportStatus newStatus = request.get("status");
            Report report = reportService.updateReportStatus(reportId, newStatus);
            log.debug("Report status updated: {} -> {}", reportId, newStatus);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            log.warn("Report not found for update: {}", reportId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating report status: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        log.debug("Received DELETE /reports/{}", reportId);
        try {
            reportService.deleteReport(reportId);
            log.debug("Report deleted: {}", reportId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting report: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete all report records for a user (GDPR compliance).
     * DELETE /api/reports/user/{userUUID}
     * 
     * Deletes all report records where the user is either the initiator (reporter)
     * or target (reported profile).
     * Creates an audit log entry for compliance tracking.
     * 
     * @param userUUID the user whose report records should be deleted
     * @return 200 OK with deletion summary containing count and audit log ID
     */
    @DeleteMapping("/user/{userUUID}")
    public ResponseEntity<Map<String, Object>> deleteUserData(@PathVariable UUID userUUID) {
        log.info("Received DELETE /reports/user/{} - initiating user data deletion", userUUID);
        try {
            int deletedCount = reportService.deleteAllUserData(userUUID);

            // Retrieve the audit log entry for this deletion
            AuditLog auditLog = auditLogService.logDeletion(userUUID, "REPORT", deletedCount);

            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedCount);
            response.put("auditLogId", auditLog.getId());
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "Successfully deleted " + deletedCount + " report records for user " + userUUID);

            log.info("Successfully deleted {} report records for user: {}", deletedCount, userUUID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during delete user data for reports: {}", e.getMessage(), e);
            throw e;
        }
    }
}
