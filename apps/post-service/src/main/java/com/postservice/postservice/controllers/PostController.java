package com.postservice.postservice.controllers;

import com.postservice.postservice.dao.GymRepository;
import com.postservice.postservice.dao.MediaInstanceRepository;
import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.services.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final MediaInstanceRepository mediaInstanceRepository;
    private final GymRepository gymRepository;
    private final PostDAO postDAO;

    public PostController(PostService postService, MediaInstanceRepository mediaInstanceRepository, GymRepository gymRepository, PostDAO postDAO) {
        this.postService = postService;
        this.mediaInstanceRepository = mediaInstanceRepository;
        this.gymRepository = gymRepository;
        this.postDAO = postDAO;
    }

    // RequestMapping, because @PutMapping and @PostMapping can't be put under each other
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public Post save(@RequestBody PostDTO postDTO){
        Set<MediaInstance> mediaInstanceSet = new HashSet<>();
        Post post = postDTO.toEntity(); 
        post.setMediaInstanceSet(this.postService.saveAsSetMediaInstanceFromSetUUID(mediaInstanceSet, postDTO.mediaInstanceSet, post));
        this.postDAO.save(post);
        return this.postDAO.getPostById(post.getId());
    }

    @GetMapping("/users/{user_id}")
    public Set<Post> getAllPostsByUserId(@PathVariable UUID user_id) {
        return this.postDAO.getPostByUserId(user_id);
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<Post> getPostById(@PathVariable UUID post_id) {
        return ResponseEntity.ok(this.postDAO.getPostById(post_id));
    }

    @DeleteMapping("/{post_id}")
    public void deletePostById(@PathVariable UUID post_id){
        this.postDAO.deletePostById(post_id);
    }
}
