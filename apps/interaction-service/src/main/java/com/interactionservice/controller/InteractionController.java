package com.interactionservice.controller;

import com.interactionservice.dto.BatchInteractionDTO;
import com.interactionservice.dto.CommentDTO;
import com.interactionservice.dto.InteractionReturnDTO;
import com.interactionservice.model.Comment;
import com.interactionservice.model.CustomLike;
import com.interactionservice.repository.CommentRepository;
import com.interactionservice.repository.LikeRepository;
import com.interactionservice.service.CommentService;
import com.interactionservice.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interaction")
public class InteractionController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private CommentRepository commentRepository;

    /**
     * Toggle a like to a post or comment.
     *
     * @param userId,      UUID of the user
     * @param referenceId, UUID of the post or comments that was liked
     * @return True if the like now exists, false if the like was deleted
     */
    @PutMapping("/{userId}/{referenceId}")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable UUID userId,
            @PathVariable UUID referenceId) {
        boolean likeNowExists = likeService.toggleLike(userId, referenceId);
        return ResponseEntity.ok(likeNowExists);
    }

    @GetMapping("/{userId}/{referenceId}")
    public ResponseEntity<Boolean> hasUserLiked(@PathVariable UUID userId, @PathVariable UUID referenceId) {
        return ResponseEntity.ok(likeService.doesLikeExist(userId, referenceId));
    }

    /**
     * Count the likes of the given post or comment identifier
     * Retrieve the comments that are linked to it
     *
     * @param referenceId, UUID of the post or comment
     * @return Integer -> number of likes
     */
    @GetMapping("/{referenceId}")
    public ResponseEntity<InteractionReturnDTO> getInteractionFromReference(@PathVariable UUID referenceId) {
        int likes = likeService.countLikesOfReference(referenceId);
        List<Comment> commentList = commentService.getAllComments(referenceId);
        InteractionReturnDTO dto = new InteractionReturnDTO(likes, commentList);
        System.out.println("Interaction was fetched for reference: " + referenceId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Creating a new comment
     *
     * @param commentDTO, Object with userId, referenceId and the commentContent
     * @return UUID of the created Comment
     */
    @PostMapping
    public ResponseEntity<UUID> createComment(@RequestBody CommentDTO commentDTO) {
        Comment commentObject = commentService.createComment(commentDTO);
        return ResponseEntity.ok(commentObject.getId());
    }

    /**
     * Deleting a comment or post
     *
     * @param referenceId, The identifier of the comment or post itself
     * @return UUID of the deleted comment or post
     */
    @DeleteMapping("/{referenceId}")
    public ResponseEntity<UUID> deleteReference(@PathVariable UUID referenceId) {
        UUID deletedCommentId = commentService.deleteComment(referenceId);
        return ResponseEntity.ok(deletedCommentId);
    }

    /**
     * Get a list of user identifiers of users that have liked the users post or
     * comment.
     * Should only be used by the owner of the reference
     *
     * @param referenceId, identifier of the post or comment
     * @return list of user identifiers that are connected via likes to the given
     *         reference
     */
    @GetMapping("likes/{referenceId}")
    public ResponseEntity<List<UUID>> getUsersThatLiked(@PathVariable UUID referenceId) {
        List<UUID> users = likeService.getUsersThatLiked(referenceId);
        return ResponseEntity.ok(users);
    }

    /**
     * Bulk fetch interactions for a list of reference IDs (post IDs).
     * Uses 2 DB queries total (one for likes, one for comments) regardless of batch size.
     * Called by the gateway's getPosts to avoid N per-post round-trips.
     */
    @PostMapping("/batch")
    public ResponseEntity<List<BatchInteractionDTO>> batchGetInteractions(
            @RequestBody List<UUID> referenceIds) {
        if (referenceIds == null || referenceIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // 1 DB query: all likes for all requested references
        Map<UUID, Long> likeCountByRef = likeRepository
                .findAllByReferenceIdIn(referenceIds)
                .stream()
                .collect(Collectors.groupingBy(CustomLike::getReferenceId, Collectors.counting()));

        // 1 DB query: all comments for all requested references
        Map<UUID, List<Comment>> commentsByRef = commentRepository
                .findAllByReferenceIdIn(referenceIds)
                .stream()
                .collect(Collectors.groupingBy(Comment::getReferenceId));

        List<BatchInteractionDTO> result = referenceIds.stream()
                .map(id -> new BatchInteractionDTO(
                        id,
                        likeCountByRef.getOrDefault(id, 0L).intValue(),
                        commentsByRef.getOrDefault(id, List.of())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Bulk check: which of a given set of reference IDs has a specific user liked?
     * Returns the set of referenceIds the user has liked — 1 DB query regardless of batch size.
     * Called by the gateway to populate the "liked" state for every post on the feed in one shot.
     */
    @PostMapping("/likes/batch")
    public ResponseEntity<Set<UUID>> batchDoesUserLike(
            @RequestParam("userId") UUID userId,
            @RequestBody List<UUID> referenceIds) {
        if (referenceIds == null || referenceIds.isEmpty()) {
            return ResponseEntity.ok(Set.of());
        }
        Set<UUID> liked = likeRepository
                .findAllByUserIdAndReferenceIdIn(userId, referenceIds)
                .stream()
                .map(CustomLike::getReferenceId)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(liked);
    }

    /**
     * Deletes all comments from the given user
     * and deletes all likes by the given user
     * Comments are deleted cascadingly through the comments and likes
     *
     * @param userId, identifier of the user to be deleted
     * @return UUID of the user that was deleted
     */
    @DeleteMapping("user/{userId}")
    public ResponseEntity<UUID> deleteUser(@PathVariable UUID userId) {
        likeService.deleteAllByUser(userId);
        commentService.deleteAllByUser(userId);
        return ResponseEntity.ok(userId);
    }
}
