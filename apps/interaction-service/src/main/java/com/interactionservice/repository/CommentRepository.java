package com.interactionservice.repository;

import com.interactionservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    // Retrieve a single comment
    Optional<Comment> getCommentByIdIs(UUID id);

    // Get all comments from a single reference
    List<Comment> getAllByReferenceId(UUID id);

    // Delete all comments that are connected to the given user
    List<Comment> findByUserId(UUID userId);
}
