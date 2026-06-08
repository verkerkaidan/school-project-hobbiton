package com.apigateway.apigateway;

import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.service.MediaService;
import com.apigateway.apigateway.service.PostAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:19293"})
public class PostsController {

    private final PostAggregationService postService;
    private final MediaService mediaService;

    public PostsController(PostAggregationService postService, MediaService mediaService) {
        this.postService = postService;
        this.mediaService = mediaService;
    }

    private static final Logger log = LoggerFactory.getLogger(PostsController.class);

    @SchemaMapping(typeName = "Post", field = "mediaSet")
    public List<MediaDTO> mediaSet(Post post) {
        if (post.getMediaInstanceSet() == null || post.getMediaInstanceSet().size() == 0) {
            return new ArrayList<MediaDTO>();
        }
        return mediaService.getMedia(post.getMediaInstanceSet());
    }
    @SchemaMapping(typeName = "Post", field = "interaction")
    public Interaction interaction(Post post) {
        if (post.getId() == null) {
            log.warn("Post has null ID, skipping interaction fetch");
            return null;
        }
        return postService.getInteraction(post.getId());
    }
    @QueryMapping
    public List<Post> getPostsByHobbyType(@Argument HobbyType hobbyType) {
        List<Post> postSet = new ArrayList<>(postService.getPostsByHobbyType(hobbyType));
        postSet.forEach(p -> log.info("Post class = {}", p.getClass().getName()));
        return postSet;
    }

    @QueryMapping
    public List<Post> getPosts() {
        List<Post> postSet = new ArrayList<>(postService.getAllPosts());
        return postSet;
    }

    @QueryMapping
    public Post getPostById(@Argument UUID id) {
        return postService.getPostById(id);
    }

    @ResponseBody
    @PostMapping(value = "/create-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createPost(
            @RequestPart("data") PostDTO dto,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files
    ) {
        dto.mediaInstanceDTOSet = files;
        Object response = postService.forwardCreatePost(dto);
        return ResponseEntity.ok(response);
    }
}
