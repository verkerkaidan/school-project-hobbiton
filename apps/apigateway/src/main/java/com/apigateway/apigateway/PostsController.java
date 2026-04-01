package com.apigateway.apigateway;

import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.DTO.MediaDTO;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.gym.Gym;
import com.apigateway.apigateway.service.PostAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Controller
public class PostsController {

    private final PostAggregationService service;

    public PostsController(PostAggregationService service) {
        this.service = service;
    }

    private static final Logger log = LoggerFactory.getLogger(PostsController.class);

    @SchemaMapping
    public List<MediaDTO> mediaSet (Post post) {
        return service.getMedia(post.getMediaInstanceSet());
    }

    @QueryMapping
    public List<Post> getPostsByHobbyType(@Argument HobbyType hobbyType) {
        List<Post> postSet = new ArrayList<>(service.getPostsByHobbyType(hobbyType));
        postSet.forEach(p -> log.info("Post class = {}", p.getClass().getName()));
        return postSet;
    }
    @QueryMapping
    public List<Post> getPosts() {
        List<Post> postSet = new ArrayList<>(service.getAllPosts());


        return postSet;
    }

    @PostMapping("/create-post")
    public Post createPost(){
        return new Gym();
    }
}
