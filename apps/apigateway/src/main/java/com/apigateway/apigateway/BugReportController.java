package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.BugReportClient;
import com.apigateway.apigateway.models.BugReportDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User-facing bug report endpoints.
 * All paths are relative to the gateway's context-path (/api):
 * POST /api/bug-reports
 *
 * The Authorization header is forwarded to the bug-report-service so it can
 * validate that the submitter is logged in (reports are still anonymous —
 * no user identity is stored in the service).
 */
@RestController
@RequestMapping("/bug-reports")
@CrossOrigin(origins = { "http://localhost:*", "http://*.student.inf.st.hsleiden.nl:*" })
public class BugReportController {

    private final BugReportClient bugReportClient;

    public BugReportController(BugReportClient bugReportClient) {
        this.bugReportClient = bugReportClient;
    }

    /** Submit a new bug report. Requires a valid Bearer token. */
    @PostMapping
    public ResponseEntity<BugReportDTO> createBugReport(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> body) {
        BugReportDTO report = bugReportClient.createBugReport(authorization, body);
        return ResponseEntity.status(201).body(report);
    }
}
