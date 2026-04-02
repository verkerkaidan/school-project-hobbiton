package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "media-service", url="${media.service.url}")
public interface MediaClient {

    @GetMapping("/media/file/{instanceId}")
    MediaDTO getMediaByInstanceId(@PathVariable UUID instanceId);

    @GetMapping("/media")
    List<ContentFlagUpdateDTO> getMediaFilesWithUnassignedFlag();

    @PutMapping("/media")
    List<UUID> assignContentFlags(@RequestBody List<ContentFlagUpdateDTO> updatedDTOList);
}
