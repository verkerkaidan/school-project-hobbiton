package com.postservice.postservice.controllers;

import com.postservice.postservice.dao.GymRepository;
import com.postservice.postservice.dao.MediaInstanceRepository;
import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.services.PostService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;


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

    /**
     * RequestMapping, because @PutMapping and @PostMapping can't be put under each other
     * Updates or creates post
     * @param postDTO
     * @return ResponseEntity with post inside it
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Post> save(@RequestBody PostDTO postDTO) {
        Post post;

        if (postDTO.id != null) {
            post = this.postDAO.getPostById(postDTO.id);
            post.setTitle(postDTO.title);
            post.setDescription(postDTO.description);
            post.setUserId(postDTO.userId);
        } else {
            post = postDTO.toEntity();
        }

        Set<MediaInstance> mediaInstanceSet = new HashSet<>();
        post.setMediaInstanceSet(this.postService.saveAsSetMediaInstanceFromSetUUID(mediaInstanceSet, postDTO.mediaInstanceSet, post));

        this.postDAO.save(post);
        return ResponseEntity.ok(this.postDAO.getPostById(post.getId()));
    }

    /**
     * Gets all posts by the user id, if user doesn't exist, 0 fetches.
     * @param user_id
     * @return
     */
    @GetMapping("/users/{user_id}")
    public ResponseEntity<Set<Post>> getAllPostsByUserId(@PathVariable UUID user_id) {
        return ResponseEntity.ok(this.postDAO.getPostByUserId(user_id));
    }

    /**
     * Gets post by postId, always singular
     * @param post_id
     * @return
     */
    @GetMapping("/{post_id}")
    public ResponseEntity<Post> getPostById(@PathVariable UUID post_id) {
        return ResponseEntity.ok(this.postDAO.getPostById(post_id));
    }

    /**
     * Gets all posts by hobbyProperty, by example GYM or READING
     * @param propertyType
     * @return
     */
    @GetMapping("/hobby/{propertyType}")
    public ResponseEntity<List<? extends Post>> getPostsByHobbyProperty(
            @PathVariable String propertyType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pagesize){
        Pageable pageable = PageRequest.of(page, pagesize);

        return ResponseEntity.ok(this.postDAO.getPostsByHobbyProperty(propertyType, pageable));
    }

    /**
     * Delete the post by id, always executes delete query even if post does not exist
     * @param post_id
     */
    @DeleteMapping("/{post_id}")
    public ResponseEntity<UUID> deletePostById(@PathVariable UUID post_id){
        this.postDAO.deletePostById(post_id);

        return ResponseEntity.ok(post_id);
    }
}
