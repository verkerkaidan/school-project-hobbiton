package com.interactionservice.controller;

import com.interactionservice.dto.CommentDTO;
import com.interactionservice.dto.InteractionReturnDTO;
import com.interactionservice.model.Comment;
import com.interactionservice.service.CommentService;
import com.interactionservice.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/interaction")
public class InteractionController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private CommentService commentService;

    /**
     * Toggle a like to a post or comment.
     *
     * @param userId, UUID of the user
     * @param referenceId, UUID of the post or comments that was liked
     * @return True if the like now exists, false if the like was deleted
     */
    @PutMapping("/{userId}/{referenceId}")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable UUID userId,
            @PathVariable UUID referenceId
    ) {
        boolean likeNowExists = likeService.toggleLike(userId, referenceId);
        return ResponseEntity.ok(likeNowExists);
    }

    /**
     * Count the likes of the given post or comment identifier
     * Retrieve the comments that are linked to it
     *
     * @param referenceId, UUID of the post or comment
     * @return Integer -> number of likes
     */
    @GetMapping("/{referenceId}")
    public ResponseEntity<InteractionReturnDTO> getLikesFromReference(@PathVariable UUID referenceId) {
        int likes = likeService.countLikesOfReference(referenceId);
        List<Comment> commentList = commentService.getAllComments(referenceId);
        InteractionReturnDTO dto = new InteractionReturnDTO(likes, commentList);
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
     * Get a list of user identifiers of users that have liked the users post or comment.
     * Should only be used by the owner of the reference
     *
     * @param referenceId, identifier of the post or comment
     * @return list of user identifiers that are connected via likes to the given reference
     */
    @GetMapping("likes/{referenceId}")
    public ResponseEntity<List<UUID>> getUsersThatLiked(@PathVariable UUID referenceId) {
        List<UUID> users = likeService.getUsersThatLiked(referenceId);
        return ResponseEntity.ok(users);
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
