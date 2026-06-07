package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.models.BugReportDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "bug-report-service", url = "${bug-report.service.url}")
public interface BugReportClient {

    /**
     * Submit a new bug report. Forwards the Authorization header so the
     * bug-report-service can validate that the submitter is logged in.
     */
    @PostMapping("/bug-reports")
    BugReportDTO createBugReport(
            @RequestHeader("Authorization") String authorization,
            @RequestBody java.util.Map<String, String> body);

    /** Fetch all reports, optionally filtered by status (OPEN / CLOSED). */
    @GetMapping("/bug-reports")
    List<BugReportDTO> getBugReports(
            @RequestParam(value = "status", required = false) String status);

    /** Close a specific report by ID. */
    @PutMapping("/bug-reports/{id}/close")
    BugReportDTO closeBugReport(@PathVariable("id") String id);
}
