package com.postservice.postservice.dao;

import com.postservice.postservice.models.reading.Reading;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, UUID> {
    List<Reading> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
