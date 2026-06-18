package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Set<Post> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    List<? extends Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Set<Post> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT p FROM Post p WHERE p.userId NOT IN :excludeIds ORDER BY p.createdAt DESC")
    List<? extends Post> findAllExcludingUsersOrderByCreatedAtDesc(@Param("excludeIds") Collection<UUID> excludeIds, Pageable pageable);
}
