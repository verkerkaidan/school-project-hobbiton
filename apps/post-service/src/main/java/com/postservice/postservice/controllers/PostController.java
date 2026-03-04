package com.postservice.postservice.controllers;

import com.postservice.postservice.dao.MediaInstanceRepository;
import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.services.PostService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/posts")
public class PostController {
    private PostDAO postDAO;

    private PostService postService;
    private MediaInstanceRepository mediaInstanceRepository;
    public PostController(PostDAO postDAO, PostService postService, MediaInstanceRepository mediaInstanceRepository) {
        this.postDAO = postDAO;
        this.postService = postService;
        this.mediaInstanceRepository = mediaInstanceRepository;
    }

    /**
     * Function to generate a post
     *
     * @param postDTO
     * @return ReponseEntity of Post
     */
    @PostMapping
    public ResponseEntity<Post> generatePost(@RequestBody PostDTO postDTO){
        Post post = new Post(postDTO);
        post.setMediaInstanceSet(this.postService.saveAsSetMediaInstanceFromSetUUID(post.getMediaInstanceSet(), postDTO.mediaInstanceSet, post));

        Post savedPost = this.postDAO.generatePost(post);
        System.out.println(savedPost.getMediaInstanceSet().size());

        return ResponseEntity.ok(savedPost);
    }

    @GetMapping("/users/{user_id}")
    public ResponseEntity<Set<Post>> getAllPostsByUserId(@PathVariable UUID user_id){
        return ResponseEntity.ok(this.postDAO.getPostsByUserId(user_id));
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<Post> getPostById(@PathVariable UUID post_id){
        return ResponseEntity.ok(this.postDAO.getPostByPostId(post_id));
    }

    @PutMapping()
    public ResponseEntity<Post> updatePost(@RequestBody PostDTO postDTO){
        Post post = this.postDAO.getPostByPostId(postDTO.id);
        post = this.postService.updatePost(postDTO, post);
        Post updatedPost = this.postDAO.generatePost(post);

        return ResponseEntity.ok(updatedPost);
    }
}
