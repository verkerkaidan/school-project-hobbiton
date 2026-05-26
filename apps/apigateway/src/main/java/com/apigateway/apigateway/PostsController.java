package com.apigateway.apigateway;

import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.modelDtos.media.MediaDTO;
import com.apigateway.apigateway.models.Post;
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

@RestController
@Controller
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:19293"})
public class PostsController {

    private final PostAggregationService service;

    public PostsController(PostAggregationService service) {
        this.service = service;
    }

    private static final Logger log = LoggerFactory.getLogger(PostsController.class);

    @SchemaMapping(typeName = "Post", field = "mediaSet")
    public List<MediaDTO> mediaSet(Post post) {
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

    @QueryMapping
    public Post getPostById(@Argument UUID id) {
        return service.getPostById(id);
    }

    @PostMapping(value = "/create-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createPost(
            @RequestPart("data") PostDTO dto,
            @RequestPart("files[]") List<MultipartFile> files
    ) {
        dto.mediaInstanceDTOSet = files;
        System.out.println("Length of the files list that was sent: " + files.size());
        Object response = service.forwardCreatePost(dto);
        return ResponseEntity.ok(response);
    }
}
