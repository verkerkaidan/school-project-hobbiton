package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import com.apigateway.apigateway.modelDtos.media.DeleteMediaRequestDTO;
import com.apigateway.apigateway.modelDtos.media.FullMediaFile;
import com.apigateway.apigateway.modelDtos.media.ReportCreationDTO;
import com.apigateway.apigateway.models.Post;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:19293" })
public class ReportController {
    private final MediaClient mediaClient;

    public ReportController(MediaClient mediaClient) {
        this.mediaClient = mediaClient;
    }

    @PostMapping("/media/report/{user_id}/{instance_id}")
    public ResponseEntity<UUID> reportFile(@PathVariable("user_id") UUID userId, @PathVariable("instance_id") UUID instanceId) {
        ReportCreationDTO dto = mediaClient.reportFile(userId, instanceId);
        return ResponseEntity.ok(dto.report_id);
    }

    @ResponseBody
    @PutMapping("/media/report")
    public ResponseEntity<String> assignContentFlag(
            @RequestBody ContentFlagUpdateDTO contentFlagUpdateDTO
    ) {
        List<ContentFlagUpdateDTO> contentFlags = new ArrayList<>();
        contentFlags.add(contentFlagUpdateDTO);
        mediaClient.assignContentFlags(contentFlags);
        return ResponseEntity.ok("ContentFlag updated");
    }

    @DeleteMapping("/media")
    public ResponseEntity<String> deleteMediaFile(
            @RequestBody DeleteMediaRequestDTO request
            ) {
        mediaClient.deleteMediaFile(request);
        return ResponseEntity.ok("Media file deleted");
    }

    @QueryMapping
    public List<FullMediaFile> getReportedFiles() {
        return mediaClient.getReportedFiles();
    }
}
