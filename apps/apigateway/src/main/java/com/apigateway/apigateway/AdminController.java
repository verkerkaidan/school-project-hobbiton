package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.clients.ProfileConnectionClient;
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
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = { "http://localhost:*", "http://*.student.inf.st.hsleiden.nl:*" })
public class AdminController {

    private final ProfileConnectionClient profileConnectionClient;
    private final PostClient postClient;

    public AdminController(ProfileConnectionClient profileConnectionClient, PostClient postClient) {
        this.profileConnectionClient = profileConnectionClient;
        this.postClient = postClient;
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
}
