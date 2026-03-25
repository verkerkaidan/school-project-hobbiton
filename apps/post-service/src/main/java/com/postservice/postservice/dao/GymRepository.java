package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.gym.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface GymRepository extends JpaRepository<Gym, UUID> {
    List<Gym> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
