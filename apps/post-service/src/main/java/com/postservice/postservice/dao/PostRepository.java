package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
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
}
