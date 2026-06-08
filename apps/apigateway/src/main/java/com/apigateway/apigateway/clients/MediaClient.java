package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.media.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "media-service", url="${media.service.url}")
public interface MediaClient {
    @GetMapping("/media/file")
    List<MediaDTO> getMediaByInstanceIds(@RequestParam("instances[]") List<UUID> instances);

    @GetMapping("/media")
    List<ContentFlagUpdateDTO> getMediaFilesWithUnassignedFlag();

    @PutMapping("/media")
    void assignContentFlags(@RequestBody List<ContentFlagUpdateDTO> updatedDTOList);

    @PostMapping("/report/{user_id}/{instance_id}")
    ReportCreationDTO reportFile(@PathVariable("user_id") UUID userId, @PathVariable("instance_id") UUID instanceId);

    @GetMapping("/report")
    List<FullMediaFile> getReportedFiles();

    @DeleteMapping("/media/admin")
    DeleteMediaResponseDTO deleteMediaFile(@RequestBody DeleteMediaRequestDTO request);

    @DeleteMapping("media/file/{instanceId}")
    DeleteMediaResponseDTO deleteMediaInstance(@PathVariable UUID instanceId);
}
