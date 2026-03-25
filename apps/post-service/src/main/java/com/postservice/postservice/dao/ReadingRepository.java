package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.reading.Reading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReadingRepository extends JpaRepository<Reading, UUID> {
    List<Reading> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
