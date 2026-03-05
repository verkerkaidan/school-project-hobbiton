package com.mediaservice.repository;

import com.mediaservice.model.MediaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaInstanceRepository extends JpaRepository<MediaInstance, UUID> {
    List<MediaInstance> getAllByFileId(UUID fileId);

    List<MediaInstance> getAllByUserId(UUID userId);

    Optional<MediaInstance> getByInstanceId(UUID instanceID);
}
