package com.postservice.postservice.dao;

import com.postservice.postservice.models.MediaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaInstanceRepository extends JpaRepository<MediaInstance, UUID> {
}
