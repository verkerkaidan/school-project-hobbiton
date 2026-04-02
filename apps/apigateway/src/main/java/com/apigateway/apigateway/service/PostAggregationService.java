package com.apigateway.apigateway.service;

import com.apigateway.apigateway.DTO.posts.PostDTO;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.DTO.MediaDTO;
import com.apigateway.apigateway.models.MediaInstance;
import com.apigateway.apigateway.models.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class PostAggregationService {

    @Value("${post.service.url}")
    private String postServiceUrl;

    private final PostClient postClient;
    private final MediaClient mediaClient;
    private final WebClient webClient;

    public PostAggregationService(PostClient postClient, MediaClient mediaClient, WebClient webClient) {
        this.postClient = postClient;
        this.mediaClient = mediaClient;
        this.webClient = webClient;
    }

    public List<Post> getPostsByHobbyType(HobbyType hobbyType) {
        return postClient.getPostsByHobbyType(hobbyType);
    }
    public List<Post> getAllPosts() {
        return postClient.getAllPosts();
    }

    public List<MediaDTO> getMedia(List<MediaInstance> instanceIds) {
        List<MediaDTO> mediaDTOList = new ArrayList<MediaDTO>();
        for (MediaInstance instance : instanceIds) {
            mediaDTOList.add(mediaClient.getMediaByInstanceId(instance.instanceId));
        }

        System.out.println(mediaDTOList.size());

        return mediaDTOList;
    }

    public Object forwardCreatePost(PostDTO dto) {
        // Just forward the dto as-is to the post microservice
        return webClient.post()
                .uri(postServiceUrl + "/posts")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Post getPostById(UUID id) {
        return postClient.getPostById(id);
    }
}