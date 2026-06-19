package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import com.apigateway.apigateway.modelDtos.media.DeleteMediaRequestDTO;
import com.apigateway.apigateway.modelDtos.media.FullMediaFile;
import com.apigateway.apigateway.modelDtos.media.ReportCreationDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.apigateway.apigateway.service.ProfileAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileAggregationService.class);

    private final MediaClient mediaClient;

    public ReportController(MediaClient mediaClient) {
        this.mediaClient = mediaClient;
    }

    @PostMapping("/media/report/{user_id}/{instance_id}")
    public ResponseEntity<UUID> reportFile(
        @PathVariable("user_id") UUID userId,
        @PathVariable("instance_id") UUID instanceId,
        @RequestHeader("Authorization") String authorization
    ) {
        try {
            ReportCreationDTO dto = mediaClient.reportFile(userId, instanceId);
            return ResponseEntity.ok(dto.report_id);
        } catch (Exception e) {
            logger.error("[REPORTCONTROLLER reportFile] Could not reach mediaServiceElx --- \n{}", e.getMessage());
            throw new RuntimeException("Responsible service could not be reached");
        }
    }

    @ResponseBody
    @PutMapping("/media/report")
    public ResponseEntity<String> assignContentFlag(
        @RequestBody ContentFlagUpdateDTO contentFlagUpdateDTO,
        @RequestHeader("Authorization") String authorization
    ) {
        List<ContentFlagUpdateDTO> contentFlags = new ArrayList<>();
        contentFlags.add(contentFlagUpdateDTO);
        try {
            mediaClient.assignContentFlags(contentFlags);
            return ResponseEntity.ok("ContentFlag updated");
        } catch (Exception e) {
            logger.error("[REPORTCONTROLLER assignContentFlag] Could not reach mediaServiceElx --- \n{}", e.getMessage());
            throw new RuntimeException("Responsible service could not be reached");
        }
    }

    @DeleteMapping("/media")
    public ResponseEntity<String> deleteMediaFile(
        @RequestBody DeleteMediaRequestDTO request,
        @RequestHeader("Authorization") String authorization
    ) {
        try {
            mediaClient.deleteMediaFile(request);
            return ResponseEntity.ok("Media file deleted");
        } catch (Exception e) {
            logger.error("[REPORTCONTROLLER deleteMediaFile] Could not reach mediaServiceElx --- \n{}", e.getMessage());
            throw new RuntimeException("Responsible service could not be reached");
        }
    }

    @QueryMapping
    public List<FullMediaFile> getReportedFiles(
            @RequestHeader("Authorization") String authorization) {
        try {
            return mediaClient.getReportedFiles();
        } catch (Exception e) {
            logger.error("[REPORTCONTROLLER getReportedFiles] Could not retrieve reportedFiles --- \n{}", e.getMessage());
            return new ArrayList<FullMediaFile>();
        }
    }
}
