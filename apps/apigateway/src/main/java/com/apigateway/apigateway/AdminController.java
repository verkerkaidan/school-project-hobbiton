package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.BugReportClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.clients.ProfileConnectionClient;
import com.apigateway.apigateway.models.AdminProfileSummaryResponse;
import com.apigateway.apigateway.models.BugReportDTO;
import com.apigateway.apigateway.models.ReportDTO;
import com.apigateway.apigateway.service.ProfileAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Admin-only REST endpoints exposed by the API Gateway.
 * The gateway forwards calls to the appropriate microservices via Feign.
 * These endpoints should all be behind an Admin
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
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final ProfileConnectionClient profileConnectionClient;
    private final ProfileClient profileClient;
    private final PostClient postClient;
    private final BugReportClient bugReportClient;
    private final ProfileAggregationService profileAggregationService;

    public AdminController(ProfileConnectionClient profileConnectionClient,
                           ProfileClient profileClient,
                           PostClient postClient,
                           BugReportClient bugReportClient,
                           ProfileAggregationService profileAggregationService) {
        this.profileConnectionClient = profileConnectionClient;
        this.profileClient = profileClient;
        this.postClient = postClient;
        this.bugReportClient = bugReportClient;
        this.profileAggregationService = profileAggregationService;
    }

    /**
     * Fetch reports filtered by status, or ALL reports in a single call.
     * Passing "ALL" as the status returns every report regardless of status.
     * Reports are enriched with initiatorUsername and targetUsername via a single
     * batch call to the profile-service — so the total service calls are:
     *   frontend → gateway (1) → profile-connection-service (1) + profile-service (1) → frontend (1)
     */
    @GetMapping("/reports/status/{status}")
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(@PathVariable String status) {
        List<ReportDTO> reports = "ALL".equalsIgnoreCase(status)
                ? profileConnectionClient.getAllReports()
                : profileConnectionClient.getReportsByStatus(status);
        return ResponseEntity.ok(enrichWithUsernames(reports));
    }

    /**
     * Bulk-enriches a list of reports with usernames for initiator and target.
     * Collects all unique user UUIDs, fetches their profiles in a single batch
     * request to the profile-service, then annotates each report.
     */
    private List<ReportDTO> enrichWithUsernames(List<ReportDTO> reports) {
        if (reports == null || reports.isEmpty()) return Collections.emptyList();

        List<UUID> uniqueIds = reports.stream()
                .flatMap(r -> Stream.of(r.getInitiator(), r.getTarget()))
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> {
                    try { return UUID.fromString(id); } catch (IllegalArgumentException e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, String> usernameMap;
        try {
            List<AdminProfileSummaryResponse> profiles = profileClient.getProfilesByIds(uniqueIds);
            usernameMap = profiles.stream()
                    .filter(p -> p.userId != null && p.username != null)
                    .collect(Collectors.toMap(p -> p.userId, p -> p.username));
        } catch (Exception e) {
            logger.warn("[AdminController enrichWithUsernames] Could not batch-fetch usernames: {}", e.getMessage());
            usernameMap = Collections.emptyMap();
        }

        final Map<String, String> lookup = usernameMap;
        reports.forEach(r -> {
            r.setInitiatorUsername(lookup.getOrDefault(r.getInitiator(), r.getInitiator()));
            r.setTargetUsername(lookup.getOrDefault(r.getTarget(), r.getTarget()));
        });

        return reports;
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
        List<AdminProfileSummaryResponse> profiles = profileAggregationService.getAllProfiles();
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
