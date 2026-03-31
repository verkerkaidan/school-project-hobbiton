package com.mediaservice.repository;

import com.mediaservice.model.ReportedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportedFileRepository extends JpaRepository<ReportedFile, UUID> {
    boolean existsByHash(byte[] hash);
}
