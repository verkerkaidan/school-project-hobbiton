package com.postservice.postservice.dao;

import com.postservice.postservice.models.other.Other;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OtherRepository extends JpaRepository<Other, UUID> {
    List<Other> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
