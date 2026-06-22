package com.postservice.postservice.controllers;

import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dto.LinkedHobbyItemDTO;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.LinkedHobbyItem;
import com.postservice.postservice.models.MediaInstance;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.services.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

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
            // UPDATE: update basic fields and replace linked items
            log.info("Updating post | postId={} userId={}", postDTO.id, postDTO.userId);
            post = this.postDAO.getPostById(postDTO.id);
            post.setTitle(postDTO.title);
            post.setDescription(postDTO.description);
            post.setUserId(postDTO.userId);

            post.getLinkedItems().clear();
            if (postDTO.linkedItems != null) {
                for (LinkedHobbyItemDTO dto : postDTO.linkedItems) {
                    LinkedHobbyItem item = new LinkedHobbyItem();
                    item.setDisplayName(dto.displayName);
                    item.setImageUrl(dto.imageUrl);
                    item.setExternalId(dto.externalId);
                    item.setHobbyType(dto.hobbyType);
                    item.setPost(post);
                    post.getLinkedItems().add(item);
                }
                log.debug("Rebuilt linked items on update | postId={} count={}", postDTO.id, postDTO.linkedItems.size());
            }
        } else {
            // CREATE: use constructor — it already maps linkedItems correctly
            // (sets displayName, imageUrl, externalId, hobbyType, post reference)
            log.info("Creating new post | userId={}", postDTO.userId);
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
                log.debug("Mapped linked items on create | count={}", linked.size());
            }
            post.setLinkedItems(linked);
        }

        // For updates, pass the existing Hibernate-managed collection so it is
        // mutated in-place. Replacing it with a new HashSet detaches the
        // PersistentSet and triggers an "orphan collection" error.
        Set<MediaInstance> mediaInstanceSet = postDTO.id != null
                ? post.getMediaInstanceSet()
                : new HashSet<>();
        post.setMediaInstanceSet(
                this.postService.saveAsSetMediaInstanceFromSetUUID(
                        mediaInstanceSet, postDTO.mediaInstanceSet, post
                )
        );

        this.postDAO.save(post);
        Post saved = this.postDAO.getPostById(post.getId());
        log.info("Post saved successfully | postId={} userId={}", saved.getId(), saved.getUserId());
        return ResponseEntity.ok(saved);
    }
    /**
     * Gets all posts by the user id, if user doesn't exist, 0 fetches.
     *
     * @param user_id get all posts by user
     * @return Return a ResponseEntity with a set (list) of posts by user
     */
    @GetMapping("/users/{user_id}")
    public ResponseEntity<Set<Post>> getAllPostsByUserId(@PathVariable UUID user_id) {
        log.debug("Fetching posts by userId | userId={}", user_id);
        Set<Post> posts = this.postDAO.getPostByUserId(user_id);
        log.info("Fetched posts by userId | userId={} count={}", user_id, posts.size());
        return ResponseEntity.ok(posts);
    }

    /**
     * Gets post by postId, always singular
     *
     * @param post_id fetch by post id
     * @return if post is found, return post, else throw error in postDAO
     */
    @GetMapping("/{post_id}")
    public ResponseEntity<Post> getPostById(@PathVariable UUID post_id) {
        log.debug("Fetching post by id | postId={}", post_id);
        Post post = this.postDAO.getPostById(post_id);
        log.info("Fetched post | postId={} userId={}", post.getId(), post.getUserId());
        return ResponseEntity.ok(post);
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
        log.debug("Fetching posts by hobby | type={} page={} pagesize={}", propertyType, page, pagesize);
        Pageable pageable = PageRequest.of(page, pagesize);
        List<? extends Post> posts = this.postDAO.getPostsByHobbyProperty(propertyType, pageable);
        log.info("Fetched posts by hobby | type={} count={} page={}", propertyType, posts.size(), page);
        return ResponseEntity.ok(posts);
    }

    @GetMapping()
    public ResponseEntity<List<? extends Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pagesize,
            @RequestParam(required = false) List<UUID> excludeUserIds) {
        Pageable pageable = PageRequest.of(page, pagesize);
        List<? extends Post> posts;

        if (excludeUserIds != null && !excludeUserIds.isEmpty()) {
            log.debug("Fetching posts excluding users | excludeCount={} page={}", excludeUserIds.size(), page);
            posts = this.postDAO.getPostsExcluding(excludeUserIds, pageable);
        } else {
            log.debug("Fetching all posts | page={} pagesize={}", page, pagesize);
            posts = this.postDAO.getPosts(pageable);
        }

        log.info("Fetched posts | count={} page={} excluded={}", posts.size(), page,
                excludeUserIds != null ? excludeUserIds.size() : 0);
        return ResponseEntity.ok(posts);
    }

    /**
     * Delete the post by id, always executes delete query even if post does not
     * exist
     *
     * @param post_id delete post by post id
     */
    @DeleteMapping("/{post_id}")
    public ResponseEntity<UUID> deletePostById(@PathVariable UUID post_id) {
        log.info("Deleting post | postId={}", post_id);
        this.postDAO.deletePostById(post_id);
        log.info("Post deleted | postId={}", post_id);
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
        log.info("Deleting all posts by userId | userId={}", user_id);
        this.postDAO.deletePostsByUserId(user_id);
        log.info("All posts deleted for userId | userId={}", user_id);
        return ResponseEntity.ok().build();
    }
}