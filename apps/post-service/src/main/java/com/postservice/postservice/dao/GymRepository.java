package com.postservice.postservice.dao;

import com.postservice.postservice.models.gym.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GymRepository extends JpaRepository<Gym, UUID> {
}
