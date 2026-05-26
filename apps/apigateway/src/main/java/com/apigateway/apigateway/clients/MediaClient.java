package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
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
}
