package com.profileconnectionservice.controller;

import com.profileconnectionservice.controller.dto.CreateReportRequest;
import com.profileconnectionservice.model.Report;
import com.profileconnectionservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody CreateReportRequest request) {
        try {
            Report report = reportService.createReport(request.getInitiatorId(), request.getTargetId(),
                    request.getReason(), request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<Report> getReport(@PathVariable UUID reportId) {
        Optional<Report> report = reportService.getReportById(reportId);
        return report.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<Report>> getReportsByTarget(@PathVariable UUID targetId) {
        List<Report> reports = reportService.getReportsByTarget(targetId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/initiator/{initiatorId}")
    public ResponseEntity<List<Report>> getReportsByInitiator(@PathVariable UUID initiatorId) {
        List<Report> reports = reportService.getReportsByInitiator(initiatorId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Report>> getReportsByStatus(@PathVariable Report.ReportStatus status) {
        List<Report> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports() {
        List<Report> reports = reportService.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{reportId}/status")
    public ResponseEntity<Report> updateReportStatus(
            @PathVariable UUID reportId,
            @RequestBody Map<String, Report.ReportStatus> request) {
        try {
            Report.ReportStatus newStatus = request.get("status");
            Report report = reportService.updateReportStatus(reportId, newStatus);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
