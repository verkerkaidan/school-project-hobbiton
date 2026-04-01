package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.models.Media;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "Media-service", url="${media.service.url}")
public interface MediaClient {

    @GetMapping("/media/file/{instanceId}")
    Media getMediaByInstanceId(@PathVariable UUID instanceId);
}
