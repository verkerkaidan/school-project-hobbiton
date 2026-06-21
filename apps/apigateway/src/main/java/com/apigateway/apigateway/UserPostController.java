package com.apigateway.apigateway;

import com.apigateway.apigateway.clients.InteractionClient;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.PostClient;
import com.apigateway.apigateway.config.JwtUtil;
import com.apigateway.apigateway.modelDtos.posts.PostDTO;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.media.MediaInstance;
import com.apigateway.apigateway.service.PostAggregationService;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User-facing REST endpoints for post operations that require ownership checks.
 * DELETE /api/posts/{postId} — authenticated via JWT Bearer token.
 */
@RestController
@RequestMapping("/posts")
public class UserPostController {

    private static final Logger log = LoggerFactory.getLogger(UserPostController.class);

    private final PostClient postClient;
    private final MediaClient mediaClient;
    private final JwtUtil jwtUtil;
    private final PostAggregationService postAggregationService;
    private final InteractionClient interactionClient;

    public UserPostController(PostClient postClient, MediaClient mediaClient, JwtUtil jwtUtil, PostAggregationService postAggregationService, InteractionClient interactionClient) {
        this.postClient = postClient;
        this.mediaClient = mediaClient;
        this.jwtUtil = jwtUtil;
        this.postAggregationService = postAggregationService;
        this.interactionClient = interactionClient;
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> updateOwnPost(
            @PathVariable UUID postId,
            @RequestPart("data") PostDTO dto,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("[UserPostController] PUT /posts/{} called", postId);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorization.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
        } catch (JwtException e) {
            log.warn("[UserPostController] JWT validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userIdClaim = claims.get("userId", String.class);
        if (userIdClaim == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID requestingUserId;
        try {
            requestingUserId = UUID.fromString(userIdClaim);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Post post;
        try {
            post = postClient.getPostById(postId);
        } catch (FeignException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        if (!requestingUserId.equals(post.getUserId())) {
            log.warn("[UserPostController] Ownership check failed: requester={} owner={}", requestingUserId,
                    post.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Post updated = postAggregationService.forwardUpdatePost(postId, dto, files, authorization);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteOwnPost(
            @PathVariable UUID postId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("[UserPostController] DELETE /posts/{} called", postId);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("[UserPostController] Missing or malformed Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorization.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
            log.info("[UserPostController] Token valid, subject={}", claims.getSubject());
        } catch (JwtException e) {
            log.warn("[UserPostController] JWT validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userIdClaim = claims.get("userId", String.class);
        if (userIdClaim == null) {
            log.warn("[UserPostController] JWT has no 'userId' claim, subject='{}'", claims.getSubject());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID requestingUserId;
        try {
            requestingUserId = UUID.fromString(userIdClaim);
        } catch (IllegalArgumentException e) {
            log.warn("[UserPostController] 'userId' claim is not a valid UUID: {}", userIdClaim);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Post post;
        try {
            post = postClient.getPostById(postId);
            log.info("[UserPostController] Fetched post {}, owner={}", postId, post.getUserId());
        } catch (FeignException.NotFound e) {
            log.warn("[UserPostController] Post {} not found", postId);
            return ResponseEntity.notFound().build();
        } catch (FeignException e) {
            log.error("[UserPostController] Feign error fetching post {}: status={} body={}",
                    postId, e.status(), e.contentUTF8());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        if (!requestingUserId.equals(post.getUserId())) {
            log.warn("[UserPostController] Ownership check failed: requester={} owner={}", requestingUserId,
                    post.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<UUID> references = new ArrayList<>();
            references.add(postId);
            interactionClient.deleteAllByReferences(references);
        } catch (Exception e) {
            log.error("[USERpOSTcONTROLLER deleteOwnPost] Interaction service could not be reached --- \n{}", e.getMessage());
        }

        try {
            postClient.deletePost(postId);
            if (post.getMediaInstanceSet() != null) {
                List<UUID> uuids = post.getMediaInstanceSet().stream().map((MediaInstance instance) -> instance.instanceId).toList();
                if (!uuids.isEmpty()) {
                    mediaClient.deleteMediaInstances(uuids);
                }
            }
            log.info("[UserPostController] Post {} deleted successfully", postId);
        } catch (FeignException e) {
            log.error("[UserPostController] Feign error deleting post {}: status={} body={}",
                    postId, e.status(), e.contentUTF8());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        return ResponseEntity.noContent().build();
    }
}
