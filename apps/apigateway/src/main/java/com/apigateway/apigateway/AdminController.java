package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.BugReportClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.clients.ProfileConnectionClient;
import com.apigateway.apigateway.models.AdminProfileSummaryResponse;
import com.apigateway.apigateway.models.BugReportDTO;
import com.apigateway.apigateway.models.ReportDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin-only REST endpoints exposed by the API Gateway.
 * The gateway forwards calls to the appropriate microservices via Feign.
 *
 * All paths are relative to the gateway's context-path (/api), so the
 * full client-side URLs are:
 * GET /api/admin/reports/status/{status}
 * PUT /api/admin/reports/{reportId}/status
 * DELETE /api/admin/posts/{postId}
 * DELETE /api/admin/posts/users/{userId}
 * GET /api/admin/profiles
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = { "http://localhost:*", "http://*.student.inf.st.hsleiden.nl:*" })
public class AdminController {

    private final ProfileConnectionClient profileConnectionClient;
    private final PostClient postClient;
    private final ProfileClient profileClient;
    private final BugReportClient bugReportClient;

    public AdminController(ProfileConnectionClient profileConnectionClient, PostClient postClient,
            ProfileClient profileClient, BugReportClient bugReportClient) {
        this.profileConnectionClient = profileConnectionClient;
        this.postClient = postClient;
        this.profileClient = profileClient;
        this.bugReportClient = bugReportClient;
    }

    /**
     * Fetch all reports with the given status (PENDING, UNDER_REVIEW, RESOLVED,
     * DISMISSED, ACTION_TAKEN).
     */
    @GetMapping("/reports/status/{status}")
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(@PathVariable String status) {
        List<ReportDTO> reports = profileConnectionClient.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    /** Update the status of a single report. Body: { "status": "RESOLVED" } */
    @PutMapping("/reports/{reportId}/status")
    public ResponseEntity<ReportDTO> updateReportStatus(
            @PathVariable UUID reportId,
            @RequestBody Map<String, String> body) {
        ReportDTO updated = profileConnectionClient.updateReportStatus(reportId, body);
        return ResponseEntity.ok(updated);
    }

    /** Hard-delete a single post by ID. */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        postClient.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    /** Hard-delete all posts belonging to a user. */
    @DeleteMapping("/posts/users/{userId}")
    public ResponseEntity<Void> deleteAllUserPosts(@PathVariable UUID userId) {
        postClient.deleteAllUserPosts(userId);
        return ResponseEntity.noContent().build();
    }

    /** Fetch all user profiles with lightweight fields for admin viewing. */
    @GetMapping("/profiles")
    public ResponseEntity<List<AdminProfileSummaryResponse>> getAllProfiles() {
        List<AdminProfileSummaryResponse> profiles = profileClient.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    /** Fetch all bug reports, optionally filtered by status (OPEN / CLOSED). */
    @GetMapping("/bug-reports")
    public ResponseEntity<List<BugReportDTO>> getBugReports(
            @RequestParam(value = "status", required = false) String status) {
        List<BugReportDTO> reports = bugReportClient.getBugReports(status);
        return ResponseEntity.ok(reports);
    }

    /** Close a bug report by ID (sets status to CLOSED). */
    @PutMapping("/bug-reports/{id}/close")
    public ResponseEntity<BugReportDTO> closeBugReport(@PathVariable String id) {
        BugReportDTO report = bugReportClient.closeBugReport(id);
        return ResponseEntity.ok(report);
    }
}
