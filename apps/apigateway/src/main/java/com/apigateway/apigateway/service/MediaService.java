package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.modelDtos.media.MediaUploadResponse;
import com.apigateway.apigateway.models.MediaInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {

    @Value("${media.service.url}")
    private String mediaServiceUrl;
    private final WebClient webClient;
    private final MediaClient mediaClient;

    public MediaService(WebClient webClient, MediaClient mediaClient) {
        this.webClient = webClient;
        this.mediaClient = mediaClient;
    }

    /**
     * Handles the uploading of media and returns the list of instances Id's
     */
    public Set<UUID> uploadMedia(List<MultipartFile> files, UUID owner_id) {
        Set<UUID> mediaInstanceSet = new HashSet<>();

        MultiValueMap<String, HttpEntity<?>> multipartBody = new LinkedMultiValueMap<>();

        files.forEach((file) -> {
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
            multipartBody.add("files[]", new HttpEntity<>(file.getResource(), fileHeaders));
        });

        multipartBody.add("owner_id", new HttpEntity<>(owner_id.toString()));

        MediaUploadResponse response = webClient.post()
                .uri(mediaServiceUrl + "/media/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(multipartBody)
                .retrieve()
                .bodyToMono(MediaUploadResponse.class)
                .block();

        if (response != null) {
            response.results.stream()
                    .filter(r -> "ok".equals(r.status))
                    .map(r -> r.id)
                    .forEach(mediaInstanceSet::add);
        }
        for (UUID id : mediaInstanceSet) {
            System.out.println("Retrieved instanceId: " + id);
        }
        return mediaInstanceSet;
    }

    /**
     * retrieve all mediaFiles from a list of instances
     * Retrieves the same file multiple times, if the instances refer to the same
     * file
     */
    public List<MediaDTO> getMedia(List<MediaInstance> instances) {
        List<UUID> instanceIds = instances.stream().map((MediaInstance instance) -> {
            return instance.instanceId;
        }).toList();
        List<MediaDTO> mediaDTOList = mediaClient.getMediaByInstanceIds(instanceIds);
        System.out.println("Retrieving media. Number of files: " + mediaDTOList.size());

        return mediaDTOList;
    }
}
