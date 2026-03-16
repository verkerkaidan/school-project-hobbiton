package com.profileconnectionservice.service;

import com.profileconnectionservice.model.Report;
import com.profileconnectionservice.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

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
}