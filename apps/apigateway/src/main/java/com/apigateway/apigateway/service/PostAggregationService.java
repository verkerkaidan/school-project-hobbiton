package com.apigateway.apigateway.service;

import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.DTO.MediaDTO;
import com.apigateway.apigateway.models.MediaInstance;
import com.apigateway.apigateway.models.Post;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostAggregationService {

    private final PostClient postClient;
    private final MediaClient mediaClient;
    // private final ReadingFeignClient readingClient; // when ready

    public PostAggregationService(PostClient postClient, MediaClient mediaClient) {
        this.postClient = postClient;
        this.mediaClient = mediaClient;
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
            System.out.println(instance.instanceId);
            mediaDTOList.add(mediaClient.getMediaByInstanceId(instance.instanceId));
        }

        System.out.println(mediaDTOList.size());

        return mediaDTOList;
    }
}