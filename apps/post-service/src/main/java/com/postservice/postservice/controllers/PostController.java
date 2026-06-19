package com.postservice.postservice.controllers;

import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dto.LinkedHobbyItemDTO;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.LinkedHobbyItem;
import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.services.PostService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostDAO postDAO;

    public PostController(PostService postService, PostDAO postDAO) {
        this.postService = postService;
        this.postDAO = postDAO;
    }

    /**
     * RequestMapping, because @PutMapping and @PostMapping can't be put under each
     * other
     * Updates or creates post
     * 
     * @param postDTO body of post that is to be made or updated
     * @return ResponseEntity with post inside it
     */
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<Post> save(@RequestBody PostDTO postDTO) {
        Post post;

        if (postDTO.id != null) {
            // UPDATE: only update basic fields
            post = this.postDAO.getPostById(postDTO.id);
            post.setTitle(postDTO.title);
            post.setDescription(postDTO.description);
            post.setUserId(postDTO.userId);
        } else {
            // CREATE: use constructor — it already maps linkedItems correctly
            // (sets displayName, imageUrl, externalId, hobbyType, post reference)
            post = postDTO.toEntity();

            // Re-map linked items here so we can be explicit and not rely
            // solely on the constructor — avoids any stale/detached state issues
            Set<LinkedHobbyItem> linked = new HashSet<>();
            if (postDTO.linkedItems != null) {
                for (LinkedHobbyItemDTO dto : postDTO.linkedItems) {
                    LinkedHobbyItem item = new LinkedHobbyItem();
                    item.setDisplayName(dto.displayName);
                    item.setImageUrl(dto.imageUrl);
                    item.setExternalId(dto.externalId);
                    item.setHobbyType(dto.hobbyType);
                    item.setPost(post);
                    linked.add(item);
                }
            }
            post.setLinkedItems(linked);
        }

        Set<MediaInstance> mediaInstanceSet = new HashSet<>();
        post.setMediaInstanceSet(
                this.postService.saveAsSetMediaInstanceFromSetUUID(
                        mediaInstanceSet, postDTO.mediaInstanceSet, post
                )
        );

        this.postDAO.save(post);
        return ResponseEntity.ok(this.postDAO.getPostById(post.getId()));
    }

    /**
     * Gets all posts by the user id, if user doesn't exist, 0 fetches.
     * 
     * @param user_id get all posts by user
     * @return Return a ResponseEntity with a set (list) of posts by user
     */
    @GetMapping("/users/{user_id}")
    public ResponseEntity<Set<Post>> getAllPostsByUserId(@PathVariable UUID user_id) {
        return ResponseEntity.ok(this.postDAO.getPostByUserId(user_id));
    }

    /**
     * Gets post by postId, always singular
     * 
     * @param post_id fetch by post id
     * @return if post is found, return post, else throw error in postDAO
     */
    @GetMapping("/{post_id}")
    public ResponseEntity<Post> getPostById(@PathVariable UUID post_id) {
        return ResponseEntity.ok(this.postDAO.getPostById(post_id));
    }

    /**
     * Gets all posts by hobbyProperty, by example GYM or READING
     * 
     * @param propertyType
     * @return
     */
    @GetMapping("/hobby/{propertyType}")
    public ResponseEntity<List<? extends Post>> getPostsByHobbyProperty(
            @PathVariable String propertyType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pagesize) {
        Pageable pageable = PageRequest.of(page, pagesize);
        return ResponseEntity.ok(this.postDAO.getPostsByHobbyProperty(propertyType, pageable));
    }

    @GetMapping()
    public ResponseEntity<List<? extends Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pagesize,
            @RequestParam(required = false) List<UUID> excludeUserIds) {
        Pageable pageable = PageRequest.of(page, pagesize);
        if (excludeUserIds != null && !excludeUserIds.isEmpty()) {
            return ResponseEntity.ok(this.postDAO.getPostsExcluding(excludeUserIds, pageable));
        }
        return ResponseEntity.ok(this.postDAO.getPosts(pageable));
    }

    /**
     * Delete the post by id, always executes delete query even if post does not
     * exist
     * 
     * @param post_id delete post by post id
     */
    @DeleteMapping("/{post_id}")
    public ResponseEntity<UUID> deletePostById(@PathVariable UUID post_id) {
        this.postDAO.deletePostById(post_id);
        return ResponseEntity.ok(post_id);
    }

    /**
     * Delete based on user_id
     * 
     * @param user_id userId that is to be deleted
     * @return ResponseEntity 200, response is ok
     */
    @DeleteMapping("/users/{user_id}")
    public ResponseEntity<?> deleteAllPostsByUserId(@PathVariable UUID user_id) {
        this.postDAO.deletePostsByUserId(user_id);
        return ResponseEntity.ok().build();
    }
}