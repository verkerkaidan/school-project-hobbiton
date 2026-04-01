package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.gym.Gym;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Set<Post> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    List<? extends Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
