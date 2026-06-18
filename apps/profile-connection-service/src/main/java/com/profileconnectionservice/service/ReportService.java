package com.profileconnectionservice.service;

import com.profileconnectionservice.model.Report;
import com.profileconnectionservice.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Report createReport(UUID initiatorId, UUID targetId, Report.ReportReason reason, String description) {
        if (initiatorId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot report yourself");
        }

        Report report = new Report(initiatorId, targetId, reason, description);
        return reportRepository.save(report);
    }

    public List<Report> getReportsByTarget(UUID targetId) {
        return reportRepository.findByTarget(targetId);
    }

    public List<Report> getReportsByInitiator(UUID initiatorId) {
        return reportRepository.findByInitiator(initiatorId);
    }

    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatus(status);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(UUID reportId) {
        return reportRepository.findById(reportId);
    }

    public Report updateReportStatus(UUID reportId, Report.ReportStatus newStatus) {
        Optional<Report> report = reportRepository.findById(reportId);
        if (report.isPresent()) {
            Report r = report.get();
            r.setStatus(newStatus);
            return reportRepository.save(r);
        }
        throw new IllegalArgumentException("Report not found");
    }

    public void deleteReport(UUID reportId) {
        reportRepository.deleteById(reportId);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus(Report.ReportStatus.PENDING);
    }

    /**
     * Delete all report records associated with a user (both as initiator and
     * target).
     * This is used for GDPR compliance and right-to-be-forgotten requests.
     * 
     * @param userUUID the user whose report records should be deleted
     * @return the total number of report records deleted
     */
    public int deleteAllUserData(UUID userUUID) {
        log.info("Starting deletion of all report data for user: {}", userUUID);
        try {
            // Delete all reports where the user is the initiator (reporter)
            int deletedAsInitiator = reportRepository.deleteByInitiator(userUUID);
            log.trace("Deleted {} reports where user was initiator", deletedAsInitiator);

            // Delete all reports where the user is the target (reported profile)
            int deletedAsTarget = reportRepository.deleteByTarget(userUUID);
            log.trace("Deleted {} reports where user was target", deletedAsTarget);

            int totalDeleted = deletedAsInitiator + deletedAsTarget;
            log.debug("Total reports deleted for user {}: {}", userUUID, totalDeleted);

            // Log the deletion for audit trail
            String details = String.format("Deleted as initiator: %d, as target: %d", deletedAsInitiator,
                    deletedAsTarget);
            auditLogService.logDeletion(userUUID, "REPORT", totalDeleted, details);

            return totalDeleted;
        } catch (Exception e) {
            log.error("Error deleting report data for user {}: {}", userUUID, e.getMessage(), e);
            throw e;
        }
    }
}