package com.postservice.postservice.dao;

import com.postservice.postservice.models.gym.Gym;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, UUID> {
    List<Gym> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
