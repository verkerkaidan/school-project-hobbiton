package com.apigateway.apigateway.service;

import com.apigateway.apigateway.modelDtos.media.MediaEntryDTO;
import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
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
    @Value("${media.service.url}")
    private String mediaServiceUrl;

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

    /**
    * retrieve all mediaFiles from a list of instances
    * Retrieves the same file multiple times, if the instances refer to the same file
    */
    public List<MediaDTO> getMedia(List<MediaInstance> instanceIds) {
        List<MediaDTO> mediaDTOList = new ArrayList<>();
        for (MediaInstance instance : instanceIds) {
            System.out.println(instance.instanceId);
            MediaDTO dto = mediaClient.getMediaByInstanceId(instance.instanceId);
            dto.setRelevantId(instance.instanceId);
            mediaDTOList.add(dto);
        }

        return mediaDTOList;
    }

    /**
     * Forwards the create post from frontend to mediaservice to postservice
     *
     * @param dto PostDTO with a list of datauri's for media
     * @return Post with a list of instance uuids for media
     */
    public Object forwardCreatePost(PostDTO dto) {
        Set<UUID> mediaInstanceSet = new HashSet<>();
        dto.mediaInstanceDTOSet.forEach((instance) ->
            mediaInstanceSet.add(webClient.post()
                .uri(mediaServiceUrl + "/media/" + dto.userId)
                .bodyValue(new MediaEntryDTO(instance))
                .retrieve()
                .bodyToMono(UUID.class)
                .block()
            )
        );

        dto.mediaInstanceSet = mediaInstanceSet;
        // Just forward the dto with instance UUIDs to the post microservice
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