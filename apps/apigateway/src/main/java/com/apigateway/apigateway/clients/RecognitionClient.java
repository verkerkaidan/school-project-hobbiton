package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "recognition-service", url="${recognition.service.url}")
public interface RecognitionClient {
    @PostMapping("/recognition")
    List<ContentFlagUpdateDTO> assignContentFlags(@RequestBody List<ContentFlagUpdateDTO> DTOToBeUpdatedList);
}
