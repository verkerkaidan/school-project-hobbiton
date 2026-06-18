package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.models.CreateReportRequest;
import com.apigateway.apigateway.models.ReportDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Feign client for the profile-connection-service, which manages social graph operations
// (follows, blocks, reports). The service URL is injected from application properties.
@FeignClient(name = "profile-connection-service", url = "${profile-connection.service.url}")
public interface ProfileConnectionClient {
    // Returns the IDs of all users who follow the given userId
    @GetMapping("/follows/{userId}/followers")
    Set<UUID> getFollowerIds(@PathVariable UUID userId);

    // Returns the IDs of all users that the given userId follows
    @GetMapping("/follows/{userId}/following")
    Set<UUID> getFollowingIds(@PathVariable UUID userId);

    // Toggles the follow relationship; currentUserId is passed as a header to
    // identify the actor
    // Returns true if the relationship now exists, false if it was removed
    @PutMapping("/follows/{targetUserId}")
    Boolean toggleFollow(@PathVariable UUID targetUserId, @RequestHeader("X-Profile-Id") UUID currentUserId);

    // Toggles the block relationship; returns true if now blocked, false if
    // unblocked
    @PutMapping("/blocks/{targetProfileId}")
    Boolean toggleBlock(@PathVariable UUID targetProfileId, @RequestHeader("X-Profile-Id") UUID currentProfileId);

    // Returns the set of profile IDs that the given profileId has blocked
    @GetMapping("/blocks/{profileId}/blocked")
    Set<UUID> getBlockedIds(@PathVariable UUID profileId);

    // Submits a new abuse report to the profile-connection-service
    @PostMapping("/reports")
    void createReport(@RequestBody CreateReportRequest request);

    // Admin: fetch every report regardless of status
    @GetMapping("/reports/all")
    List<ReportDTO> getAllReports();

    // Admin: fetch all reports with a given status
    @GetMapping("/reports/status/{status}")
    List<ReportDTO> getReportsByStatus(@PathVariable String status);

    // Admin: update the status of a report
    @PutMapping("/reports/{reportId}/status")
    ReportDTO updateReportStatus(@PathVariable UUID reportId, @RequestBody Map<String, String> body);

    // Admin: hard-delete a single report record
    @DeleteMapping("/reports/{reportId}")
    void deleteReport(@PathVariable UUID reportId);
}
